package com.mydevlog.mydevlog.infrastructure.post

import com.mydevlog.mydevlog.domain.post.Post
import java.util.*

interface PostRepository {
    fun save(post: Post): Post
    fun findById(id: UUID): Post?
    fun findBySlug(slug: String): Post?
    fun deleteById(id: UUID)
    fun findAll(): List<Post>
}
