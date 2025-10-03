package com.mydevlog.mydevlog.infrastructure.post

import com.mydevlog.mydevlog.infrastructure.post.table.PostsTable
import com.mydevlog.mydevlog.infrastructure.post.table.TagsTable
import com.mydevlog.mydevlog.infrastructure.post.table.PostTagsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class TagQueryRepository {
    data class TagStat(val name: String, val postCount: Int)
    data class TagStatsResponse(val tags: List<TagStat>, val totalPosts: Int)

    fun getStats(): TagStatsResponse = transaction {
        val total = PostsTable.selectAll().count().toInt()

        // Load all tags
        val tagNames = TagsTable
            .selectAll()
            .map { it[TagsTable.name] }

        if (tagNames.isEmpty()) return@transaction TagStatsResponse(emptyList(), total)

        // Count posts per tag by reading mappings and aggregating in-memory
        val tagIdToName = TagsTable.selectAll().associate { it[TagsTable.id] to it[TagsTable.name] }
        val countsMap = mutableMapOf<String, Int>().apply { tagNames.forEach { this[it] = 0 } }
        PostTagsTable.selectAll().forEach { row ->
            val tagId = row[PostTagsTable.tagId]
            val name = tagIdToName[tagId]
            if (name != null) countsMap[name] = (countsMap[name] ?: 0) + 1
        }

        val stats = countsMap.entries
            .map { TagStat(it.key, it.value) }
            .sortedBy { it.name.lowercase() }

        TagStatsResponse(stats, total)
    }
}