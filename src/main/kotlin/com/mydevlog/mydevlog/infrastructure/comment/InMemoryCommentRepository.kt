package com.mydevlog.mydevlog.infrastructure.comment

import com.mydevlog.mydevlog.domain.comment.Comment
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryCommentRepository {
    private val byId = ConcurrentHashMap<UUID, Comment>()

    fun save(comment: Comment): Comment {
        byId[comment.id] = comment
        return comment
    }

    fun findById(id: UUID): Comment? = byId[id]

    fun deleteById(id: UUID) { byId.remove(id) }

    fun findByPostId(postId: UUID): List<Comment> = byId.values.filter { it.postId == postId }
}