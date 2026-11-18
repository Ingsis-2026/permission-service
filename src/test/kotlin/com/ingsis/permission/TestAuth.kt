package com.ingsis.permission

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor

/**
 * Un pedido de snippet-service: un access token machine-to-machine, como los que emite Auth0 con
 * client credentials. No se valida contra Auth0, así que los tests no necesitan red.
 */
fun asSnippetService(): RequestPostProcessor = jwt().jwt { it.subject("snippet-service@clients") }
