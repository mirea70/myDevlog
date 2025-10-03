package com.mydevlog.mydevlog.infrastructure.post.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object PostsTable : Table("posts") {
    val id = uuid("id").uniqueIndex()
    val slug = varchar("slug", 255).uniqueIndex()
    val title = varchar("title", 255)
    val content = text("content")
    val summary = text("summary").nullable()
    val coverImageUrl = varchar("cover_image_url", 512).nullable()
    val category = varchar("category", 255).nullable()
    val updatedAt = timestamp("updated_at")
    val createdAt = timestamp("created_at")
    val authorId = uuid("author_id")
    val featured = bool("featured")
    val readingMinutes = integer("reading_minutes")

    override val primaryKey = PrimaryKey(id)
}