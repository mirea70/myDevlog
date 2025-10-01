package com.mydevlog.mydevlog.api.comment

import com.mydevlog.mydevlog.api.comment.dto.*
import com.mydevlog.mydevlog.api.support.AdminGuard
import com.mydevlog.mydevlog.api.support.UserGuard
import com.mydevlog.mydevlog.application.comment.CommentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class CommentsController(
    private val service: CommentService,
    private val userGuard: UserGuard,
    private val adminGuard: AdminGuard,
) {

    @GetMapping("/api/posts/{slug}/comments")
    fun list(
        @PathVariable slug: String,
        @RequestParam(required = false) parentId: UUID?,
        @RequestParam(required = false) cursor: Instant?,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): List<CommentResponse> = service.listVisible(slug, parentId, cursor, pageSize).map { it.toResponse() }

    @PostMapping("/api/posts/{slug}/comments")
    fun create(
        @RequestHeader(name = "X-User-Id", required = false) userIdHeader: String?,
        @PathVariable slug: String,
        @Valid @RequestBody req: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val userId = userGuard.requireUserId(userIdHeader)
        val c = service.create(slug, userId, req.parentId, req.content)
        return ResponseEntity.status(HttpStatus.CREATED).body(c.toResponse())
    }

    @PutMapping("/api/comments/{id}")
    fun update(
        @RequestHeader(name = "X-User-Id", required = false) userIdHeader: String?,
        @RequestHeader(name = "X-Admin", required = false) adminHeader: String?,
        @PathVariable id: UUID,
        @Valid @RequestBody req: UpdateCommentRequest
    ): CommentResponse {
        val isAdmin = adminHeader?.equals("true", true) == true
        val userId = if (!isAdmin) userGuard.requireUserId(userIdHeader) else null
        return service.edit(id, userId, isAdmin, req.content).toResponse()
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader(name = "X-User-Id", required = false) userIdHeader: String?,
        @RequestHeader(name = "X-Admin", required = false) adminHeader: String?,
        @PathVariable id: UUID,
    ) {
        val isAdmin = adminHeader?.equals("true", true) == true
        val userId = if (!isAdmin) userGuard.requireUserId(userIdHeader) else null
        service.delete(id, userId, isAdmin)
    }

    @PutMapping("/api/admin/comments/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun hide(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID
    ) {
        adminGuard.requireAdmin(admin)
        service.adminHide(id)
    }

    @PutMapping("/api/admin/comments/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun restore(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID
    ) {
        adminGuard.requireAdmin(admin)
        service.adminRestore(id)
    }

    @PutMapping("/api/admin/comments/{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun block(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID
    ) {
        adminGuard.requireAdmin(admin)
        service.adminBlock(id)
    }
}
