package com.mydevlog.mydevlog.infrastructure.post.table

import org.jetbrains.exposed.sql.Table

object PostTagsTable : Table("post_tags") {
    val postId = uuid("post_id")
    val tagId = uuid("tag_id")

    override val primaryKey = PrimaryKey(postId, tagId)
}