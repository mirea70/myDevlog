package com.mydevlog.mydevlog.api.support

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.*

/**
 * Temporary user guard until JWT is implemented.
 * Requires header X-User-Id: <uuid>
 */
@Component
class UserGuard {
    fun requireUserId(header: String?): UUID {
        if (header.isNullOrBlank()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User required")
        return try { UUID.fromString(header) } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id")
        }
    }
}