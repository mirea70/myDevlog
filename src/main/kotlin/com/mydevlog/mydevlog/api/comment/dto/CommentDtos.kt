package com.mydevlog.mydevlog.api.comment.dto

import com.mydevlog.mydevlog.domain.comment.Comment
import com.mydevlog.mydevlog.domain.comment.CommentStatus
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

data class CreateCommentRequest(
    @field:Size(min = 1, max = 2000)
    val content: String,
    val parentId: UUID?
)

data class UpdateCommentRequest(
    @field:Size(min = 1, max = 2000)
    val content: String
)

data class CommentResponse(
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

fun Comment.toResponse() = CommentResponse(
    id, postId, authorId, parentId, content, status, edited, createdAt, updatedAt
)
