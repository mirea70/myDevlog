package com.mydevlog.mydevlog.api.post.dto

import com.mydevlog.mydevlog.domain.post.Post
import java.time.Instant
import java.util.*

// Response DTOs

data class PostItemResponse(
    val id: UUID,
    val slug: String,
    val title: String,
    val summary: String?,
    val coverImageUrl: String?,
    val tags: List<String>,
    val category: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
    val authorId: UUID,
    val featured: Boolean,
    val readingMinutes: Int,
    val isNew: Boolean,
)

data class PostDetailResponse(
    val id: UUID,
    val slug: String,
    val title: String,
    val content: String,
    val summary: String?,
    val coverImageUrl: String?,
    val tags: List<String>,
    val category: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
    val authorId: UUID,
    val featured: Boolean,
    val readingMinutes: Int,
    val isNew: Boolean,
)

data class PostListResponse(
    val items: List<PostItemResponse>,
    val total: Int,
    val countByCategory: Map<String, Int>,
)

// Requests (admin)

data class PostCreateRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val coverImageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val authorId: UUID,
    val featured: Boolean = false,
)

data class PostUpdateRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val coverImageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val featured: Boolean = false,
)

fun Post.toItemDto() = PostItemResponse(
    id, slug, title, summary, coverImageUrl, tags, category, updatedAt, createdAt, authorId, featured, readingMinutes, isNew
)

fun Post.toDetailDto() = PostDetailResponse(
    id, slug, title, content, summary, coverImageUrl, tags, category, updatedAt, createdAt, authorId, featured, readingMinutes, isNew
)
