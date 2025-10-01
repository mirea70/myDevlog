package com.mydevlog.mydevlog.infrastructure.comment

import com.mydevlog.mydevlog.domain.comment.Comment
import com.mydevlog.mydevlog.domain.comment.CommentStatus
import com.mydevlog.mydevlog.infrastructure.comment.table.CommentsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class ExposedCommentRepository {

    fun save(comment: Comment): Comment {
        transaction {
            val exists = CommentsTable.selectAll().where { CommentsTable.id eq comment.id }.empty().not()
            if (exists) {
                CommentsTable.update({ CommentsTable.id eq comment.id }) {
                    it[postId] = comment.postId
                    it[authorId] = comment.authorId
                    it[parentId] = comment.parentId
                    it[content] = comment.content
                    it[status] = comment.status.name
                    it[edited] = comment.edited
                    it[updatedAt] = comment.updatedAt
                    it[createdAt] = comment.createdAt
                }
            } else {
                CommentsTable.insert {
                    it[id] = comment.id
                    it[postId] = comment.postId
                    it[authorId] = comment.authorId
                    it[parentId] = comment.parentId
                    it[content] = comment.content
                    it[status] = comment.status.name
                    it[edited] = comment.edited
                    it[createdAt] = comment.createdAt
                    it[updatedAt] = comment.updatedAt
                }
            }
        }
        return comment
    }

    fun findById(id: UUID): Comment? = transaction {
        CommentsTable.selectAll().where { CommentsTable.id eq id }.singleOrNull()?.toDomain()
    }

    fun deleteSoft(id: UUID) {
        transaction {
            CommentsTable.update({ CommentsTable.id eq id }) {
                it[status] = CommentStatus.DELETED.name
                it[updatedAt] = Instant.now()
            }
        }
    }

    fun updateStatus(id: UUID, status: CommentStatus) {
        transaction {
            CommentsTable.update({ CommentsTable.id eq id }) {
                it[CommentsTable.status] = status.name
                it[updatedAt] = Instant.now()
            }
        }
    }

    fun updateContent(id: UUID, content: String) {
        transaction {
            CommentsTable.update({ CommentsTable.id eq id }) {
                it[CommentsTable.content] = content
                it[edited] = true
                it[updatedAt] = Instant.now()
            }
        }
    }

    fun listVisibleByPost(
        postId: UUID,
        parentId: UUID?,
        cursor: Instant?,
        pageSize: Int
    ): List<Comment> = transaction {
        val q = org.jetbrains.exposed.sql.SqlExpressionBuilder.run {
            var cond: Op<Boolean> = (CommentsTable.postId eq postId) and (CommentsTable.status eq CommentStatus.VISIBLE.name)
            cond = if (parentId == null) {
                cond and CommentsTable.parentId.isNull()
            } else {
                cond and (CommentsTable.parentId eq parentId)
            }
            if (cursor != null) {
                cond = cond and (CommentsTable.createdAt less cursor)
            }
            cond
        }
        CommentsTable.selectAll().where { q }
            .orderBy(CommentsTable.createdAt, SortOrder.DESC)
            .limit(pageSize)
            .map { it.toDomain() }
    }

    private fun ResultRow.toDomain(): Comment = Comment(
        id = this[CommentsTable.id],
        postId = this[CommentsTable.postId],
        authorId = this[CommentsTable.authorId],
        parentId = this[CommentsTable.parentId],
        content = this[CommentsTable.content],
        status = CommentStatus.valueOf(this[CommentsTable.status]),
        edited = this[CommentsTable.edited],
        createdAt = this[CommentsTable.createdAt],
        updatedAt = this[CommentsTable.updatedAt],
    )
}
