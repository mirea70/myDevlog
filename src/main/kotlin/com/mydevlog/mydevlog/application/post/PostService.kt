package com.mydevlog.mydevlog.application.post

import com.mydevlog.mydevlog.domain.post.Post
import com.mydevlog.mydevlog.infrastructure.post.PostRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil

@Service
class PostService(
    private val repo: PostRepository,
) {
    fun list(
        page: Int = 1,
        pageSize: Int = 10,
        category: String? = null,
        tag: String? = null,
        q: String? = null,
        sort: String? = null,
    ): List<Post> {
        var items = repo.findAll()
        if (!category.isNullOrBlank()) items = items.filter { it.category.equals(category, true) }
        if (!tag.isNullOrBlank()) items = items.filter { it.tags.any { t -> t.equals(tag, true) } }
        if (!q.isNullOrBlank()) {
            val term = q.lowercase()
            items = items.filter { it.title.lowercase().contains(term) || it.content.lowercase().contains(term) }
        }
        val sorted = when (sort) {
            "updatedAt" -> items.sortedByDescending { it.updatedAt }
            else -> items.sortedByDescending { it.createdAt }
        }
        val safePage = page.coerceAtLeast(1)
        val safeSize = pageSize.coerceIn(1, 100)
        val from = (safePage - 1) * safeSize
        return sorted.drop(from).take(safeSize)
    }

    fun listFeatured(): List<Post> = repo.findAll().filter { it.featured }

    fun getBySlug(slug: String): Post? = repo.findBySlug(slug)

    fun adminCreate(
        title: String,
        content: String,
        summary: String?,
        coverImageUrl: String?,
        tags: List<String>,
        category: String?,
        authorId: UUID,
        featured: Boolean,
    ): Post {
        val now = Instant.now()
        val slug = uniqueSlug(slugify(title))
        val readingMinutes = calcReadingMinutes(content)
        val post = Post(
            id = UUID.randomUUID(),
            slug = slug,
            title = title,
            content = content,
            summary = summary,
            coverImageUrl = coverImageUrl,
            tags = tags,
            category = category,
            updatedAt = now,
            createdAt = now,
            authorId = authorId,
            featured = featured,
            readingMinutes = readingMinutes,
        )
        return repo.save(post)
    }

    fun adminUpdate(
        id: UUID,
        title: String,
        content: String,
        summary: String?,
        coverImageUrl: String?,
        tags: List<String>,
        category: String?,
        featured: Boolean,
    ): Post {
        val existing = repo.findById(id) ?: throw IllegalArgumentException("Post not found")
        val now = Instant.now()
        val newSlug = if (existing.title != title) uniqueSlug(slugify(title)) else existing.slug
        val readingMinutes = calcReadingMinutes(content)
        val updated = existing.copy(
            slug = newSlug,
            title = title,
            content = content,
            summary = summary,
            coverImageUrl = coverImageUrl,
            tags = tags,
            category = category,
            updatedAt = now,
            featured = featured,
            readingMinutes = readingMinutes,
        )
        return repo.save(updated)
    }

    fun adminDelete(id: UUID) {
        repo.deleteById(id)
    }

    private fun slugify(input: String): String {
        val base = input.lowercase().replace(Regex("[^a-z0-9-_]+"), "-").trim('-')
        return base.ifBlank { "post" }
    }

    private fun uniqueSlug(base: String): String {
        if (repo.findBySlug(base) == null) return base
        var i = 2
        while (true) {
            val candidate = "$base-$i"
            if (repo.findBySlug(candidate) == null) return candidate
            i++
        }
    }

    private fun calcReadingMinutes(content: String): Int {
        val words = Regex("\\s+").split(content.trim()).filter { it.isNotEmpty() }.size
        return ceil(words / 230.0).toInt().coerceAtLeast(1)
    }
}