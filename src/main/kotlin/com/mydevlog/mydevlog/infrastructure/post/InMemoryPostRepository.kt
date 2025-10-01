package com.mydevlog.mydevlog.infrastructure.post

import com.mydevlog.mydevlog.domain.post.Post
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryPostRepository : PostRepository {
    private val byId = ConcurrentHashMap<UUID, Post>()
    private val bySlug = ConcurrentHashMap<String, UUID>()

    override fun save(post: Post): Post {
        byId[post.id] = post
        bySlug[post.slug] = post.id
        return post
    }

    override fun findById(id: UUID): Post? = byId[id]

    override fun findBySlug(slug: String): Post? = bySlug[slug]?.let { byId[it] }

    override fun deleteById(id: UUID) {
        val removed = byId.remove(id)
        if (removed != null) bySlug.remove(removed.slug)
    }

    override fun findAll(): List<Post> = byId.values.toList()
}