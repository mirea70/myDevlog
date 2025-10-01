package com.mydevlog.mydevlog.application.media

import com.mydevlog.mydevlog.infrastructure.media.MediaProperties
import com.mydevlog.mydevlog.infrastructure.media.InMemoryMediaRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.mock.web.MockMultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO

class MediaServiceSpec : FunSpec({

    fun pngBytes(w: Int = 8, h: Int = 8): ByteArray {
        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.color = Color.BLUE
        g.fillRect(0, 0, w, h)
        g.dispose()
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        return baos.toByteArray()
    }

    test("upload stores file and saves metadata") {
        val tmp = Files.createTempDirectory("media-test").toFile()
        val props = MediaProperties(
            storage = MediaProperties.Storage(root = tmp.absolutePath, publicBaseUrl = "https://example.com/files"),
            limits = MediaProperties.Limits(maxBytes = 5 * 1024 * 1024),
            allowedMime = listOf("image/png", "image/jpeg", "image/webp")
        )
        val repo = mockk<InMemoryMediaRepository>(relaxed = true)
        val service = MediaService(props, repo)

        val file = MockMultipartFile("file", "sample.png", "image/png", pngBytes())
        val res = service.upload(file)
        res.mime shouldBe "image/png"
        (res.bytes > 0L) shouldBe true
        res.publicUrl.startsWith("https://example.com/files/") shouldBe true

        verify { repo.save(withArg { it.mime shouldBe "image/png" }) }
        confirmVerified(repo)
    }

    test("oversized file throws 413") {
        val tmp = Files.createTempDirectory("media-test").toFile()
        val props = MediaProperties(
            storage = MediaProperties.Storage(root = tmp.absolutePath, publicBaseUrl = "https://example.com/files"),
            limits = MediaProperties.Limits(maxBytes = 1024),
            allowedMime = listOf("image/png")
        )
        val service = MediaService(props, InMemoryMediaRepository())
        val file = MockMultipartFile("file", "big.png", "image/png", ByteArray(2048) { 1 })
        val ex = try { service.upload(file); null } catch (e: MediaException) { e }
        ex!!.status shouldBe 413
    }

    test("extension mismatch throws 415") {
        val tmp = Files.createTempDirectory("media-test").toFile()
        val props = MediaProperties(
            storage = MediaProperties.Storage(root = tmp.absolutePath, publicBaseUrl = "https://example.com/files"),
            limits = MediaProperties.Limits(maxBytes = 1024 * 1024),
            allowedMime = listOf("image/png")
        )
        val service = MediaService(props, InMemoryMediaRepository())
        val file = MockMultipartFile("file", "wrong.jpg", "image/jpeg", pngBytes())
        val ex = try { service.upload(file); null } catch (e: MediaException) { e }
        ex!!.status shouldBe 415
    }
})
