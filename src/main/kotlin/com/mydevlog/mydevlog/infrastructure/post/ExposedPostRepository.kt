package com.mydevlog.mydevlog.infrastructure.post

import com.mydevlog.mydevlog.domain.post.Post
import com.mydevlog.mydevlog.infrastructure.post.table.PostsTable
import com.mydevlog.mydevlog.infrastructure.post.table.TagsTable
import com.mydevlog.mydevlog.infrastructure.post.table.PostTagsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
            // sync tags mapping
            // 1) remove existing mappings
            PostTagsTable.deleteWhere { PostTagsTable.postId eq post.id }
            // 2) ensure tags exist and map
            post.tags.map { it.trim() }.filter { it.isNotBlank() }.forEach { name ->
                val tagUuid = ensureTag(name)
                PostTagsTable.insertIgnore { st ->
                    st[PostTagsTable.postId] = post.id
                    st[PostTagsTable.tagId] = tagUuid
                }
            }
        }
        return post
    }

    override fun findById(id: UUID): Post? = transaction {
        val row = PostsTable.selectAll().where { PostsTable.id eq id }.singleOrNull()
        row?.let { toDomain(it, loadTagsForPost(id)) }
    }

    override fun findBySlug(slug: String): Post? = transaction {
        val row = PostsTable.selectAll().where { PostsTable.slug eq slug }.singleOrNull()
        row?.let { toDomain(it, loadTagsForPost(row[PostsTable.id])) }
    }

    override fun deleteById(id: UUID) {
        transaction {
            PostTagsTable.deleteWhere { PostTagsTable.postId eq id }
            PostsTable.deleteWhere { PostsTable.id eq id }
        }
    }

    override fun findAll(): List<Post> = transaction {
        val rows = PostsTable.selectAll().toList()
        val ids = rows.map { it[PostsTable.id] }
        val tagsMap = loadTagsForPosts(ids)
        rows.map { toDomain(it, tagsMap[it[PostsTable.id]] ?: emptyList()) }
    }

    private fun bind(st: UpdateBuilder<Number>, post: Post) {
        st[PostsTable.slug] = post.slug
        st[PostsTable.title] = post.title
        st[PostsTable.content] = post.content
        st[PostsTable.summary] = post.summary
        st[PostsTable.coverImageUrl] = post.coverImageUrl
        st[PostsTable.category] = post.category
        st[PostsTable.updatedAt] = post.updatedAt
        st[PostsTable.createdAt] = post.createdAt
        st[PostsTable.authorId] = post.authorId
        st[PostsTable.featured] = post.featured
        st[PostsTable.readingMinutes] = post.readingMinutes
    }

    private fun toDomain(row: ResultRow, tagNames: List<String>): Post = Post(
        id = row[PostsTable.id],
        slug = row[PostsTable.slug],
        title = row[PostsTable.title],
        content = row[PostsTable.content],
        summary = row[PostsTable.summary],
        coverImageUrl = row[PostsTable.coverImageUrl],
        tags = tagNames,
        category = row[PostsTable.category],
        updatedAt = row[PostsTable.updatedAt],
        createdAt = row[PostsTable.createdAt],
        authorId = row[PostsTable.authorId],
        featured = row[PostsTable.featured],
        readingMinutes = row[PostsTable.readingMinutes],
    )

    private fun ensureTag(name: String): UUID {
        val existing = TagsTable.selectAll().where { TagsTable.name eq name }.singleOrNull()
        if (existing != null) return existing[TagsTable.id]
        val id = UUID.randomUUID()
        TagsTable.insert { st ->
            st[TagsTable.id] = id
            st[TagsTable.name] = name
        }
        return id
    }

    private fun loadTagsForPost(postId: UUID): List<String> {
        val tagIds = PostTagsTable
            .selectAll().where { PostTagsTable.postId eq postId }
            .map { it[PostTagsTable.tagId] }
        if (tagIds.isEmpty()) return emptyList()
        return TagsTable
            .selectAll().where { TagsTable.id inList tagIds }
            .map { it[TagsTable.name] }
    }

    private fun loadTagsForPosts(postIds: List<UUID>): Map<UUID, List<String>> {
        if (postIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<UUID, MutableList<String>>()
        val mappings = PostTagsTable
            .selectAll().where { PostTagsTable.postId inList postIds }
            .map { it[PostTagsTable.postId] to it[PostTagsTable.tagId] }
        if (mappings.isEmpty()) return emptyMap()
        val tagIds = mappings.map { it.second }.distinct()
        val idToName = TagsTable
            .selectAll().where { TagsTable.id inList tagIds }
            .associate { it[TagsTable.id] to it[TagsTable.name] }
        mappings.forEach { (pid, tid) ->
            val name = idToName[tid] ?: return@forEach
            result.getOrPut(pid) { mutableListOf() }.add(name)
        }
        return result
    }
}
