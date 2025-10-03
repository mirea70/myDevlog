package com.mydevlog.mydevlog.api.tag

import com.mydevlog.mydevlog.infrastructure.post.TagQueryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TagsController(
    private val repo: TagQueryRepository,
) {
    data class TagCount(val name: String, val postCount: Int)
    data class TagStatsResponse(val tags: List<TagCount>, val totalPosts: Int)

    @GetMapping("/api/tags/stats")
    fun stats(): TagStatsResponse {
        val r = repo.getStats()
        return TagStatsResponse(r.tags.map { TagCount(it.name, it.postCount) }, r.totalPosts)
    }
}