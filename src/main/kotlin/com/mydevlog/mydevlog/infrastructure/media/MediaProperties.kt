package com.mydevlog.mydevlog.infrastructure.media

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Validated
@ConfigurationProperties(prefix = "media")
class MediaProperties(
    val storage: Storage,
    val limits: Limits,
    val allowedMime: List<String>
) {
    class Storage(
        @NotBlank val root: String,
        @NotBlank val publicBaseUrl: String,
    )
    class Limits(
        @Min(1) val maxBytes: Long,
        @DefaultValue("false") val enableVirusScan: Boolean = false
    )
}
