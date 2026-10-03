package com.ingsis.permission

import com.ingsis.permission.permissions.PermissionAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(PermissionAlreadyExistsException::class)
    fun alreadyExists(e: PermissionAlreadyExistsException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}
