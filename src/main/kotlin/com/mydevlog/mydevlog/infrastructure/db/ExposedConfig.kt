package com.mydevlog.mydevlog.infrastructure.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.springframework.context.annotation.Bean
import com.mydevlog.mydevlog.infrastructure.comment.table.CommentsTable
import com.mydevlog.mydevlog.infrastructure.post.table.PostsTable
import com.mydevlog.mydevlog.infrastructure.post.table.TagsTable
import com.mydevlog.mydevlog.infrastructure.post.table.PostTagsTable

@Configuration
class ExposedConfig {

    @Bean
    fun exposedDatabase(dataSource: DataSource): Database {
        val db = Database.connect(dataSource)
        // Auto-create comments, posts, tags tables
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(CommentsTable, PostsTable, TagsTable, PostTagsTable)
        }
        return db
    }
}
