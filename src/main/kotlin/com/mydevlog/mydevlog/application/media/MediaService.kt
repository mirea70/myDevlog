package com.mydevlog.mydevlog.application.media

import com.mydevlog.mydevlog.application.media.dto.MediaUploadResult
import com.mydevlog.mydevlog.domain.media.Media
import com.mydevlog.mydevlog.infrastructure.media.InMemoryMediaRepository
import com.mydevlog.mydevlog.infrastructure.media.MediaProperties
import org.apache.tika.Tika
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO

@Service
class MediaService(
    private val properties: MediaProperties,
    private val repo: InMemoryMediaRepository,
) {
    private val tika = Tika()
    private val root: Path = Path.of(properties.storage.root).toAbsolutePath().normalize()
    private val tempDir: Path = root.resolve("tmp")

    init {
        Files.createDirectories(tempDir)
    }

    fun upload(file: MultipartFile): MediaUploadResult {
        if (file.isEmpty) throw MediaException("Empty file", 400)
        if (file.size > properties.limits.maxBytes) throw MediaException("File too large", 413)

        val originalName = file.originalFilename ?: "file"
        val ext = safeExtension(originalName)
        val tmp = Files.createTempFile(tempDir, "upload_", ".tmp")
        file.inputStream.use { input ->
            streamToFile(input, tmp)
        }
        // Sniff MIME from content
        val detectedMime = tika.detect(tmp)
        if (properties.allowedMime.none { it.equals(detectedMime, ignoreCase = true) }) {
            Files.deleteIfExists(tmp)
            throw MediaException("Unsupported media type", 415)
        }
        // Extension must match mime family
        if (!extensionMatchesMime(ext, detectedMime)) {
            Files.deleteIfExists(tmp)
            throw MediaException("File extension does not match content type", 415)
        }

        val now = Instant.now().atZone(ZoneOffset.UTC)
        val datePath = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(now)
        val uuid = UUID.randomUUID().toString()
        val safeBase = sanitizeBaseName(originalName.substringBeforeLast('.'))
        val finalRel = "$datePath/${uuid}_${safeBase}.$ext"
        val finalPath = ensureUnderRoot(root.resolve(finalRel))
        Files.createDirectories(finalPath.parent)

        // Optionally extract image dimensions and verify it's an image
        var width: Int? = null
        var height: Int? = null
        if (detectedMime.startsWith("image/")) {
            val img = ImageIO.read(tmp.toFile())
            if (img != null) {
                width = img.width
                height = img.height
            }
        }

        // Move to final
        Files.move(tmp, finalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)

        val publicUrl = buildPublicUrl(finalRel)
        val media = Media(
            id = UUID.fromString(uuid),
            relativePath = finalRel,
            publicUrl = publicUrl,
            mime = detectedMime,
            bytes = Files.size(finalPath),
            width = width,
            height = height,
            createdAt = Instant.now(),
        )
        repo.save(media)
        return MediaUploadResult(
            id = media.id,
            publicUrl = media.publicUrl,
            mime = media.mime,
            bytes = media.bytes,
            width = media.width,
            height = media.height,
            relativePath = media.relativePath,
        )
    }

    fun delete(id: UUID) {
        val media = repo.findById(id) ?: return
        val path = ensureUnderRoot(root.resolve(media.relativePath))
        try {
            Files.deleteIfExists(path)
        } finally {
            repo.deleteById(id)
        }
    }

    private fun buildPublicUrl(relative: String): String {
        val base = properties.storage.publicBaseUrl.trimEnd('/')
        return "$base/$relative"
    }

    private fun sanitizeBaseName(name: String): String {
        val cleaned = name.lowercase().replace(Regex("[^a-z0-9-_]+"), "-").trim('-')
        return cleaned.ifBlank { "file" }.take(80)
    }

    private fun safeExtension(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        if (ext.isBlank()) return "bin"
        if (ext.contains('/') || ext.contains('\\')) return "bin"
        return ext
    }

    private fun extensionMatchesMime(ext: String, mime: String): Boolean {
        val map = mapOf(
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "webp" to "image/webp",
        )
        val expected = map[ext]
        return expected == null || expected.equals(mime, ignoreCase = true)
    }

    private fun streamToFile(input: InputStream, target: Path) {
        Files.newOutputStream(target).use { out ->
            input.copyTo(out)
        }
    }

    private fun ensureUnderRoot(path: Path): Path {
        val normalized = path.toAbsolutePath().normalize()
        if (!normalized.startsWith(root)) throw MediaException("Invalid path", 400)
        return normalized
    }
}
