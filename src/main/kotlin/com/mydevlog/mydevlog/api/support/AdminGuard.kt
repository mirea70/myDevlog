package com.mydevlog.mydevlog.api.support

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

/**
 * Temporary admin guard until proper JWT/role auth is implemented.
 * Requires header X-Admin: true
 */
@Component
class AdminGuard {
    fun requireAdmin(header: String?) {
        if (header?.equals("true", ignoreCase = true) != true) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only")
        }
    }
}
