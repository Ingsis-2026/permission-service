package com.ingsis.permission

import com.ingsis.permission.observability.REQUEST_ID_HEADER
import com.ingsis.permission.permissions.PermissionRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import java.util.UUID
import kotlin.test.assertTrue

/** Cada cambio de permisos queda en el log con el id del pedido y su contexto, que en texto va entre corchetes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension::class)
class RequestIdTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val repository: PermissionRepository,
) {
    private val snippetId = UUID.randomUUID()

    @AfterEach
    fun cleanUp() = repository.deleteAll()

    private fun grant(role: String = "OWNER") =
        mockMvc.post("/permissions") {
            with(asSnippetService())
            header(REQUEST_ID_HEADER, "abc-123")
            contentType = MediaType.APPLICATION_JSON
            content = """{"snippetId": "$snippetId", "userId": "auth0|ana", "role": "$role"}"""
        }

    private fun CapturedOutput.logLine(message: String) = out.lines().single { it.endsWith(message) }

    @Test
    fun `la respuesta lleva el id del pedido`() {
        grant().andExpect { header { string(REQUEST_ID_HEADER, "abc-123") } }
    }

    @Test
    fun `un permiso otorgado queda en el log con el snippet, el usuario, el rol y el id del pedido`(output: CapturedOutput) {
        grant()

        val line = output.logLine("Se otorgó un permiso")
        listOf("requestId=abc-123", "snippetId=$snippetId", "userId=auth0|ana", "role=OWNER").forEach { assertTrue(it in line, line) }
    }

    @Test
    fun `un permiso repetido queda en el log`(output: CapturedOutput) {
        grant()
        grant(role = "READ").andExpect { status { isConflict() } }

        assertTrue("role=READ" in output.logLine("El usuario ya tenía un permiso sobre el snippet: no se cambia"))
    }

    @Test
    fun `al borrar un snippet queda en el log cuantos permisos se revocaron`(output: CapturedOutput) {
        grant()

        mockMvc.delete("/permissions") {
            with(asSnippetService())
            param("snippetId", snippetId.toString())
        }

        assertTrue("revoked=1" in output.logLine("Se revocaron los permisos del snippet"))
    }
}
