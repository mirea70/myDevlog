package com.mydevlog.mydevlog.application.account

import com.mydevlog.mydevlog.domain.account.Account
import com.mydevlog.mydevlog.domain.account.Role
import com.mydevlog.mydevlog.domain.account.Status
import com.mydevlog.mydevlog.infrastructure.account.InMemoryAccountRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AccountService(
    private val repo: InMemoryAccountRepository,
) {
    fun getMe(userId: UUID): Account? = repo.findById(userId)

    fun updateMe(userId: UUID, name: String): Account {
        val acc = repo.findById(userId) ?: throw IllegalArgumentException("Account not found")
        val updated = acc.copy(name = name, updatedAt = Instant.now())
        return repo.save(updated)
    }

    fun adminCreate(email: String, passwordHash: String, name: String, role: Role, status: Status): Account {
        if (repo.findByEmail(email) != null) throw IllegalArgumentException("Email already exists")
        val now = Instant.now()
        val acc = Account(UUID.randomUUID(), email, passwordHash, name, role, status, now, now)
        return repo.save(acc)
    }

    fun adminUpdate(id: UUID, name: String, role: Role, status: Status): Account {
        val acc = repo.findById(id) ?: throw IllegalArgumentException("Account not found")
        val updated = acc.copy(name = name, role = role, status = status, updatedAt = Instant.now())
        return repo.save(updated)
    }

    fun adminDelete(id: UUID) { repo.deleteById(id) }

    fun findByEmail(email: String): Account? = repo.findByEmail(email)
}