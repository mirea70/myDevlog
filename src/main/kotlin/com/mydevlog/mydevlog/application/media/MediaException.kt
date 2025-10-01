package com.mydevlog.mydevlog.application.media

/**
 * Media-specific exception containing an HTTP-like status code for mapping in controllers.
 */
class MediaException(message: String, val status: Int) : RuntimeException(message)
