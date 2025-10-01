package com.mydevlog.mydevlog.domain.post

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class Post(
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
) {
    val isNew: Boolean
        get() = Instant.now().minus(7, ChronoUnit.DAYS).isBefore(createdAt)
}
