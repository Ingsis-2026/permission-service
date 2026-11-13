package com.ingsis.permission.permissions

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
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
        return repository.save(Permission(request.snippetId, request.userId, request.role)).toResponse()
    }

    /** El rol del usuario sobre el snippet, o 404 si no tiene ninguno. */
    @GetMapping("/permissions")
    fun find(
        @RequestParam snippetId: UUID,
        @RequestParam userId: String,
    ): PermissionResponse =
        repository.findBySnippetIdAndUserId(snippetId, userId)?.toResponse()
            ?: throw PermissionNotFoundException(snippetId, userId)

    /**
     * US5: los permisos de un usuario sobre todos sus snippets, para listar los que puede ver.
     * Con [role] se queda solo con los que creó (`OWNER`) o con los que le compartieron (`READ`).
     */
    @GetMapping("/permissions/snippets")
    fun findByUser(
        @RequestParam userId: String,
        @RequestParam(required = false) role: Role?,
    ): List<PermissionResponse> {
        val permissions = if (role == null) repository.findAllByUserId(userId) else repository.findAllByUserIdAndRole(userId, role)
        return permissions.map { it.toResponse() }
    }

    /**
     * Se borró el snippet: nadie tiene más permisos sobre él. Es idempotente, así que un snippet
     * sin permisos, o del que ya se borraron, también responde 204.
     */
    @DeleteMapping("/permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeAll(
        @RequestParam snippetId: UUID,
    ) {
        repository.deleteAllBySnippetId(snippetId)
    }

    private fun Permission.toResponse() = PermissionResponse(snippetId, userId, role)
}

class PermissionAlreadyExistsException(
    snippetId: UUID,
    userId: String,
) : RuntimeException("El usuario $userId ya tiene un permiso sobre el snippet $snippetId")

class PermissionNotFoundException(
    snippetId: UUID,
    userId: String,
) : RuntimeException("El usuario $userId no tiene permisos sobre el snippet $snippetId")
