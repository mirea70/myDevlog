package com.mydevlog.mydevlog.api.post

import com.mydevlog.mydevlog.api.post.dto.*
import com.mydevlog.mydevlog.api.support.AdminGuard
import com.mydevlog.mydevlog.application.post.PostService
import com.mydevlog.mydevlog.domain.post.Post
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class PostsController(
    private val posts: PostService,
    private val adminGuard: AdminGuard,
) {

    @GetMapping("/api/posts")
    fun list(
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) sort: String?,
    ): PostListResponse {
        val all = posts.list(page = 1, pageSize = Int.MAX_VALUE, category = category, tag = tag, q = q, sort = sort)
        val countByCategory = all.filter { it.category != null }
            .groupBy { it.category!! }
            .mapValues { it.value.size }
        val pageItems = posts.list(page, pageSize, category, tag, q, sort).map(Post::toItemDto)
        return PostListResponse(items = pageItems, total = all.size, countByCategory = countByCategory)
    }

    @GetMapping("/api/posts/featured")
    fun featured(): List<PostItemResponse> = posts.listFeatured().map(Post::toItemDto)

    @GetMapping("/api/posts/{slug}")
    fun getBySlug(@PathVariable slug: String): ResponseEntity<PostDetailResponse> {
        val post = posts.getBySlug(slug) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        return ResponseEntity.ok(post.toDetailDto())
    }

    @PostMapping("/api/admin/posts")
    fun adminCreate(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @RequestBody req: PostCreateRequest,
    ): ResponseEntity<PostDetailResponse> {
        adminGuard.requireAdmin(admin)
        val created = posts.adminCreate(
            title = req.title,
            content = req.content,
            summary = req.summary,
            coverImageUrl = req.coverImageUrl,
            tags = req.tags,
            category = req.category,
            authorId = req.authorId,
            featured = req.featured,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDetailDto())
    }

    @PutMapping("/api/admin/posts/{id}")
    fun adminUpdate(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID,
        @RequestBody req: PostUpdateRequest,
    ): PostDetailResponse {
        adminGuard.requireAdmin(admin)
        val updated = posts.adminUpdate(
            id = id,
            title = req.title,
            content = req.content,
            summary = req.summary,
            coverImageUrl = req.coverImageUrl,
            tags = req.tags,
            category = req.category,
            featured = req.featured,
        )
        return updated.toDetailDto()
    }

    @DeleteMapping("/api/admin/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun adminDelete(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID,
    ) {
        adminGuard.requireAdmin(admin)
        posts.adminDelete(id)
    }
}
