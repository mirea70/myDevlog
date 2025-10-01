package com.mydevlog.mydevlog.application.comment

import com.mydevlog.mydevlog.domain.comment.Comment
import com.mydevlog.mydevlog.domain.comment.CommentStatus
import com.mydevlog.mydevlog.infrastructure.comment.ExposedCommentRepository
import com.mydevlog.mydevlog.infrastructure.post.InMemoryPostRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class CommentService(
    private val repo: ExposedCommentRepository,
    private val postRepo: InMemoryPostRepository,
) {
    private val forbidden = setOf("badword", "spam")
    private val rateWindow = Duration.ofSeconds(10)
    private val lastPostMap: MutableMap<String, Instant> = ConcurrentHashMap()

    fun listVisible(postSlug: String, parentId: UUID?, cursor: Instant?, pageSize: Int): List<Comment> {
        val post = postRepo.findBySlug(postSlug) ?: throw CommentException("Post not found", 404)
        return repo.listVisibleByPost(post.id, parentId, cursor, pageSize.coerceIn(1, 100))
    }

    fun create(postSlug: String, authorId: UUID, parentId: UUID?, content: String): Comment {
        validateContent(content)
        val post = postRepo.findBySlug(postSlug) ?: throw CommentException("Post not found", 404)
        enforceRateLimit(authorId, post.id)
        val now = Instant.now()
        val c = Comment(
            id = UUID.randomUUID(),
            postId = post.id,
            authorId = authorId,
            parentId = parentId,
            content = content.trim(),
            status = CommentStatus.VISIBLE,
            edited = false,
            createdAt = now,
            updatedAt = now,
        )
        return repo.save(c)
    }

    fun edit(id: UUID, requesterId: UUID?, isAdmin: Boolean, content: String): Comment {
        validateContent(content)
        val existing = repo.findById(id) ?: throw CommentException("Comment not found", 404)
        if (!isAdmin && existing.authorId != requesterId) throw CommentException("Forbidden", 403)
        repo.updateContent(id, content.trim())
        return repo.findById(id)!!
    }

    fun delete(id: UUID, requesterId: UUID?, isAdmin: Boolean) {
        val existing = repo.findById(id) ?: return
        if (!isAdmin && existing.authorId != requesterId) throw CommentException("Forbidden", 403)
        repo.deleteSoft(id)
    }

    fun adminHide(id: UUID) = repo.updateStatus(id, CommentStatus.HIDDEN)
    fun adminRestore(id: UUID) = repo.updateStatus(id, CommentStatus.VISIBLE)
    fun adminBlock(id: UUID) = repo.updateStatus(id, CommentStatus.BLOCKED)

    private fun validateContent(content: String) {
        val text = content.trim()
        if (text.length !in 1..2000) throw CommentException("Invalid content length", 400)
        val lowered = text.lowercase()
        if (forbidden.any { lowered.contains(it) }) throw CommentException("Forbidden words used", 400)
    }

    private fun enforceRateLimit(authorId: UUID, postId: UUID) {
        val key = "$authorId:$postId"
        val now = Instant.now()
        val last = lastPostMap[key]
        if (last != null && Duration.between(last, now) < rateWindow) {
            throw CommentException("Rate limit exceeded", 429)
        }
        lastPostMap[key] = now
    }
}
