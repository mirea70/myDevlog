package com.mydevlog.mydevlog.domain.media

import java.time.Instant
import java.util.*

data class Media(
    val id: UUID,
    val relativePath: String,
    val publicUrl: String,
    val mime: String,
    val bytes: Long,
    val width: Int?,
    val height: Int?,
    val createdAt: Instant,
)
