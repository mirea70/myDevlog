package com.mydevlog.mydevlog.domain.account

import java.time.Instant
import java.util.*

data class Account(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: Role,
    val status: Status,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class Role { ADMIN, USER }

enum class Status { ACTIVE, SUSPENDED, DELETED }
