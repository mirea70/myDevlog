package com.mydevlog.mydevlog.api.media

import com.mydevlog.mydevlog.api.media.dto.UploadResponse
import com.mydevlog.mydevlog.api.support.AdminGuard
import com.mydevlog.mydevlog.application.media.MediaException
import com.mydevlog.mydevlog.application.media.MediaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/admin/media")
class AdminMediaController(
    private val mediaService: MediaService,
    private val adminGuard: AdminGuard,
) {

    @PostMapping
    fun upload(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UploadResponse> {
        adminGuard.requireAdmin(admin)
        val r = mediaService.upload(file)
        val body = UploadResponse(r.id, r.publicUrl, r.width, r.height, r.mime, r.bytes)
        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader(name = "X-Admin", required = false) admin: String?,
        @PathVariable id: UUID
    ) {
        adminGuard.requireAdmin(admin)
        mediaService.delete(id)
    }

}
