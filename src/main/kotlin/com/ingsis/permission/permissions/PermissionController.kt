package com.ingsis.permission.permissions

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class PermissionRequest(
    val snippetId: UUID,
    @field:NotBlank val userId: String,
    val role: Role,
)

data class PermissionResponse(
    val snippetId: UUID,
    val userId: String,
    val role: Role,
)

@RestController
class PermissionController(
    private val repository: PermissionRepository,
) {
    @PostMapping("/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    fun grant(
        @Valid @RequestBody request: PermissionRequest,
    ): PermissionResponse {
        if (repository.existsBySnippetIdAndUserId(request.snippetId, request.userId)) {
            throw PermissionAlreadyExistsException(request.snippetId, request.userId)
        }
        val saved = repository.save(Permission(request.snippetId, request.userId, request.role))
        return PermissionResponse(saved.snippetId, saved.userId, saved.role)
    }
}

class PermissionAlreadyExistsException(
    snippetId: UUID,
    userId: String,
) : RuntimeException("El usuario $userId ya tiene un permiso sobre el snippet $snippetId")
