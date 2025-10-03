package com.mydevlog.mydevlog.infrastructure.post.table

import org.jetbrains.exposed.sql.Table

object TagsTable : Table("tags") {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 100).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}