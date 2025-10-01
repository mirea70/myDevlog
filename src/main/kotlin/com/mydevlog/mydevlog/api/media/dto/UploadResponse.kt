package com.mydevlog.mydevlog.api.media.dto

import java.util.UUID

/**
 * Response DTO for media upload.
 */
data class UploadResponse(
    val id: UUID,
    val publicUrl: String,
    val width: Int?,
    val height: Int?,
    val mime: String,
    val bytes: Long,
)
