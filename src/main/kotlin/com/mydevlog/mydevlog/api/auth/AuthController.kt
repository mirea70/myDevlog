package com.mydevlog.mydevlog.api.auth

import com.mydevlog.mydevlog.application.auth.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val auth: AuthService,
) {
    data class LoginRequest(val email: String, val password: String)
    data class TokenResponse(val accessToken: String, val refreshToken: String)
    data class AccessTokenResponse(val accessToken: String)

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Map<String, Any>> {
        val result = auth.login(req.email, req.password)
        val body = mapOf(
            "accessToken" to result.accessToken,
            "refreshToken" to result.refreshToken,
            "account" to result.account,
        )
        return ResponseEntity.status(HttpStatus.OK).body(body)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestParam refreshToken: String): AccessTokenResponse =
        AccessTokenResponse(auth.refresh(refreshToken))

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestParam refreshToken: String) {
        auth.logout(refreshToken)
    }
}
