package com.ingsis.permission

import com.ingsis.permission.permissions.PermissionAlreadyExistsException
import com.ingsis.permission.permissions.PermissionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(PermissionAlreadyExistsException::class)
    fun alreadyExists(e: PermissionAlreadyExistsException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler(PermissionNotFoundException::class)
    fun notFound(e: PermissionNotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
}
