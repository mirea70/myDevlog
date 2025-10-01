package com.mydevlog.mydevlog.infrastructure.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.springframework.context.annotation.Bean
import com.mydevlog.mydevlog.infrastructure.comment.table.CommentsTable
import com.mydevlog.mydevlog.infrastructure.post.table.PostsTable

@Configuration
class ExposedConfig {

    @Bean
    fun exposedDatabase(dataSource: DataSource): Database {
        val db = Database.connect(dataSource)
        // Auto-create comments and posts tables
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(CommentsTable, PostsTable)
        }
        return db
    }
}
