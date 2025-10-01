package com.mydevlog.mydevlog.infrastructure.comment.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object CommentsTable : Table("comments") {
    val id = uuid("id").uniqueIndex()
    val postId = uuid("post_id").index()
    val authorId = uuid("author_id").index()
    val parentId = uuid("parent_id").nullable().index()
    val content = varchar("content", 2000)
    val status = varchar("status", 16)
    val edited = bool("edited")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}