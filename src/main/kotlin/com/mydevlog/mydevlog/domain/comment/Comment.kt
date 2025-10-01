package com.mydevlog.mydevlog.domain.comment

import java.time.Instant
import java.util.*

data class Comment(
    val id: UUID,
    val postId: UUID,
    val authorId: UUID,
    val parentId: UUID?,
    val content: String,
    val status: CommentStatus,
    val edited: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class CommentStatus { VISIBLE, HIDDEN, DELETED, BLOCKED }
