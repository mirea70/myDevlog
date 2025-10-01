package com.mydevlog.mydevlog.application.media.dto

import java.util.*

/**
 * Application-level DTO for media upload result returned by MediaService.
 */
data class MediaUploadResult(
    val id: UUID,
    val publicUrl: String,
    val mime: String,
    val bytes: Long,
    val width: Int?,
    val height: Int?,
    val relativePath: String,
)
