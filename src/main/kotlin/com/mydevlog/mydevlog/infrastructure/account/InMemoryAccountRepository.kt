package com.mydevlog.mydevlog.infrastructure.account

import com.mydevlog.mydevlog.domain.account.Account
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Repository

@Repository
class InMemoryAccountRepository {
    private val byId = ConcurrentHashMap<UUID, Account>()
    private val byEmail = ConcurrentHashMap<String, UUID>()

    fun save(account: Account): Account {
        byId[account.id] = account
        byEmail[account.email.lowercase()] = account.id
        return account
    }

    fun findById(id: UUID): Account? = byId[id]

    fun findByEmail(email: String): Account? = byEmail[email.lowercase()]?.let { byId[it] }

    fun deleteById(id: UUID) {
        byId.remove(id)?.let { byEmail.remove(it.email.lowercase()) }
    }

    fun findAll(): List<Account> = byId.values.toList()
}
