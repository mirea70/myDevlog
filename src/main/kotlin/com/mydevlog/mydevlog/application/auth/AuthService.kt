package com.mydevlog.mydevlog.application.auth

import com.mydevlog.mydevlog.application.account.AccountService
import com.mydevlog.mydevlog.domain.account.Account
import com.mydevlog.mydevlog.domain.account.Status
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val accounts: AccountService,
) {
    data class AuthResult(val accessToken: String, val refreshToken: String, val account: Account)

    fun login(email: String, password: String): AuthResult {
        val acc = accounts.findByEmail(email) ?: throw IllegalArgumentException("Invalid credentials")
        if (acc.status != Status.ACTIVE) throw IllegalArgumentException("Account inactive")
        // For demo: accept any password and issue dummy tokens
        val access = "access-${UUID.randomUUID()}"
        val refresh = "refresh-${UUID.randomUUID()}"
        return AuthResult(access, refresh, acc)
    }

    fun refresh(refreshToken: String): String {
        if (!refreshToken.startsWith("refresh-")) throw IllegalArgumentException("Invalid refresh token")
        return "access-${UUID.randomUUID()}"
    }

    fun logout(refreshToken: String) {
        // no-op for in-memory stub
    }
}