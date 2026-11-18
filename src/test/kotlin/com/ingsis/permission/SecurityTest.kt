package com.ingsis.permission

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

/** Solo snippet-service, con su token machine-to-machine, puede leer o cambiar permisos. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun `sin token no se puede dar un permiso`() {
        mockMvc
            .post("/permissions") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"snippetId": "${UUID.randomUUID()}", "userId": "auth0|intruso", "role": "OWNER"}"""
            }.andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")) }
            }
    }

    @Test
    fun `sin token no se pueden leer ni borrar permisos`() {
        val snippetId = UUID.randomUUID().toString()
        mockMvc
            .get("/permissions") {
                param("snippetId", snippetId)
                param("userId", "auth0|ana")
            }.andExpect { status { isUnauthorized() } }
        mockMvc.get("/permissions/snippets") { param("userId", "auth0|ana") }.andExpect { status { isUnauthorized() } }
        mockMvc.delete("/permissions") { param("snippetId", snippetId) }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `un token que no es Bearer no sirve`() {
        mockMvc
            .get("/permissions/snippets") {
                header(HttpHeaders.AUTHORIZATION, "Basic YW5hOjEyMzQ=")
                param("userId", "auth0|ana")
            }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `el health check no pide token`() {
        mockMvc.get("/actuator/health").andExpect { status { isOk() } }
    }
}
