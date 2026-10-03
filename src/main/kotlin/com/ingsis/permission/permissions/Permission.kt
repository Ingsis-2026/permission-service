package com.ingsis.permission.permissions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

enum class Role { OWNER, READ }

/**
 * Qué rol tiene un usuario sobre un snippet. Hay a lo sumo un rol por par snippet–usuario.
 *
 * Los ids llegan ya resueltos: este servicio no conoce los snippets ni los usuarios, y el
 * `userId` es texto porque será el `sub` del token de Auth0.
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["snippet_id", "user_id"])])
class Permission(
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
)

interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findBySnippetIdAndUserId(
        snippetId: UUID,
        userId: String,
    ): Permission?

    fun existsBySnippetIdAndUserId(
        snippetId: UUID,
        userId: String,
    ): Boolean
}
