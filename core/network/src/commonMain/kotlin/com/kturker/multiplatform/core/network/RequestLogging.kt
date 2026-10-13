package com.kturker.multiplatform.core.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal val SENSITIVE_HEADERS = listOf(
    HttpHeaders.Authorization,
    HttpHeaders.Cookie,
    HttpHeaders.SetCookie,
    HttpHeaders.ProxyAuthorization
)

private val httpLog = Logger.withTag("Http")

// Debug builds only: one line per request and response (method, URL, status), written to Kermit at
// Debug level. Release builds never install it, so no URL reaches the log there.
internal fun HttpClientConfig<*>.installRequestLogging(isDebug: Boolean) {
    if (!isDebug) return
    install(Logging) {
        level = LogLevel.INFO
        logger = object : KtorLogger {
            override fun log(message: String) {
                httpLog.d { message }
            }
        }
        sanitizeHeader { header -> SENSITIVE_HEADERS.any { it.equals(header, ignoreCase = true) } }
    }
}
