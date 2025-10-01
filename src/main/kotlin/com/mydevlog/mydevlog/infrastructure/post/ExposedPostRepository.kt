package com.mydevlog.mydevlog.infrastructure.post

import com.mydevlog.mydevlog.domain.post.Post
import com.mydevlog.mydevlog.infrastructure.post.table.PostsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Primary
class ExposedPostRepository : PostRepository {

    override fun save(post: Post): Post {
        transaction {
            val exists = PostsTable.selectAll().where { PostsTable.id eq post.id }.limit(1).any()
            if (exists) {
                PostsTable.update({ PostsTable.id eq post.id }) { st ->
                    bind(st, post)
                }
            } else {
                PostsTable.insert { st ->
                    st[PostsTable.id] = post.id
                    bind(st, post)
                }
            }
        }
        return post
    }

    override fun findById(id: UUID): Post? = transaction {
        PostsTable.selectAll().where { PostsTable.id eq id }.singleOrNull()?.let { toDomain(it) }
    }

    override fun findBySlug(slug: String): Post? = transaction {
        PostsTable.selectAll().where { PostsTable.slug eq slug }.singleOrNull()?.let { toDomain(it) }
    }

    override fun deleteById(id: UUID) {
        transaction { PostsTable.deleteWhere { PostsTable.id eq id } }
    }

    override fun findAll(): List<Post> = transaction {
        PostsTable.selectAll().map { toDomain(it) }
    }

    private fun bind(st: UpdateBuilder<Number>, post: Post) {
        st[PostsTable.slug] = post.slug
        st[PostsTable.title] = post.title
        st[PostsTable.content] = post.content
        st[PostsTable.summary] = post.summary
        st[PostsTable.coverImageUrl] = post.coverImageUrl
        st[PostsTable.tags] = post.tags.joinToString(",")
        st[PostsTable.category] = post.category
        st[PostsTable.updatedAt] = post.updatedAt
        st[PostsTable.createdAt] = post.createdAt
        st[PostsTable.authorId] = post.authorId
        st[PostsTable.featured] = post.featured
        st[PostsTable.readingMinutes] = post.readingMinutes
    }

    private fun toDomain(row: ResultRow): Post = Post(
        id = row[PostsTable.id],
        slug = row[PostsTable.slug],
        title = row[PostsTable.title],
        content = row[PostsTable.content],
        summary = row[PostsTable.summary],
        coverImageUrl = row[PostsTable.coverImageUrl],
        tags = row[PostsTable.tags].split(',').filter { it.isNotBlank() },
        category = row[PostsTable.category],
        updatedAt = row[PostsTable.updatedAt],
        createdAt = row[PostsTable.createdAt],
        authorId = row[PostsTable.authorId],
        featured = row[PostsTable.featured],
        readingMinutes = row[PostsTable.readingMinutes],
    )
}
