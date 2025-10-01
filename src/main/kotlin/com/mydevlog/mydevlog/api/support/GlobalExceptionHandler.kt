package com.mydevlog.mydevlog.api.support

import com.mydevlog.mydevlog.application.media.MediaException
import com.mydevlog.mydevlog.application.comment.CommentException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String? = null,
        val details: Map<String, String?>? = null,
    )

    @ExceptionHandler(MediaException::class)
    fun handleMedia(ex: MediaException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.status)
        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = ex.message
            )
        )
    }

    @ExceptionHandler(CommentException::class)
    fun handleComment(ex: CommentException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.status)
        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = ex.message
            )
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val status = ex.statusCode
        val reason = (status as? HttpStatus)?.reasonPhrase ?: status.toString()
        return ResponseEntity.status(status).body(
            ErrorResponse(status.value(), reason, ex.reason)
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidation(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val bindingResult = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult
            is BindException -> ex.bindingResult
            else -> null
        }
        val fieldErrors = bindingResult?.fieldErrors?.associate { it.field to it.defaultMessage }
        return ResponseEntity.status(status).body(
            ErrorResponse(status.value(), status.reasonPhrase, "Validation failed", fieldErrors)
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(
            ErrorResponse(status.value(), status.reasonPhrase, ex.message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(status).body(
            ErrorResponse(status.value(), status.reasonPhrase, ex.message)
        )
    }
}
