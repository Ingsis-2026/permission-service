package com.ingsis.permission

import com.ingsis.permission.permissions.PermissionRepository
import com.ingsis.permission.permissions.Role
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val repository: PermissionRepository,
) {
    @AfterEach
    fun cleanUp() = repository.deleteAll()

    private fun grant(
        snippetId: UUID,
        userId: String,
        role: String = "OWNER",
    ) = mockMvc.post("/permissions") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"snippetId": "$snippetId", "userId": "$userId", "role": "$role"}"""
    }

    @Test
    fun `se guarda el owner de un snippet`() {
        val snippetId = UUID.randomUUID()
        grant(snippetId, "auth0|ana").andExpect {
            status { isCreated() }
            jsonPath("$.snippetId") { value(snippetId.toString()) }
            jsonPath("$.userId") { value("auth0|ana") }
            jsonPath("$.role") { value("OWNER") }
        }
        val saved = repository.findAll().single()
        assertEquals(Role.OWNER, saved.role)
    }

    @Test
    fun `un segundo permiso para el mismo usuario y snippet es un 409`() {
        val snippetId = UUID.randomUUID()
        grant(snippetId, "auth0|ana")
        grant(snippetId, "auth0|ana", role = "READ").andExpect { status { isConflict() } }
        assertEquals(1, repository.count())
    }

    @Test
    fun `el mismo usuario puede tener permisos sobre distintos snippets`() {
        grant(UUID.randomUUID(), "auth0|ana").andExpect { status { isCreated() } }
        grant(UUID.randomUUID(), "auth0|ana").andExpect { status { isCreated() } }
    }

    @Test
    fun `un rol desconocido es un 400`() {
        grant(UUID.randomUUID(), "auth0|ana", role = "ADMIN").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `un usuario vacio es un 400`() {
        grant(UUID.randomUUID(), "").andExpect { status { isBadRequest() } }
    }

    private fun find(
        snippetId: UUID,
        userId: String,
    ) = mockMvc.get("/permissions") {
        param("snippetId", snippetId.toString())
        param("userId", userId)
    }

    @Test
    fun `se consulta el rol de un usuario sobre un snippet`() {
        val snippetId = UUID.randomUUID()
        grant(snippetId, "auth0|ana", role = "READ")
        find(snippetId, "auth0|ana").andExpect {
            status { isOk() }
            jsonPath("$.snippetId") { value(snippetId.toString()) }
            jsonPath("$.userId") { value("auth0|ana") }
            jsonPath("$.role") { value("READ") }
        }
    }

    @Test
    fun `un usuario sin permisos sobre el snippet es un 404`() {
        val snippetId = UUID.randomUUID()
        grant(snippetId, "auth0|ana")
        find(snippetId, "auth0|beto").andExpect { status { isNotFound() } }
        find(UUID.randomUUID(), "auth0|ana").andExpect { status { isNotFound() } }
    }
}
