@file:Suppress("MatchingDeclarationName") // Named after what the file sets up, not after one of its declarations.

package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.ApiError
import com.kturker.multiplatform.core.api.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import java.sql.SQLException

class ApiException(val status: HttpStatusCode, val code: ErrorCode, message: String) : RuntimeException(message)

fun notFound(): Nothing =
    throw ApiException(HttpStatusCode.NotFound, ErrorCode.NOT_FOUND, "Not found")

fun badRequest(message: String): Nothing =
    throw ApiException(HttpStatusCode.BadRequest, ErrorCode.BAD_REQUEST, message)

/**
 * What the log says about an unexpected error: exception class names along the cause chain, plus the SQLState of a
 * database error. Never the messages or stack text: a database error's message quotes the statement and the failing
 * row, which hold uids and emails.
 */
internal fun describe(e: Throwable): String {
    val chain = generateSequence(e) { it.cause }.take(MAX_CAUSES).toList()
    val classes = chain.joinToString(" <- ") { it::class.java.name }
    val sqlState = chain.filterIsInstance<SQLException>().firstNotNullOfOrNull { it.sqlState }
    return if (sqlState == null) classes else "$classes (SQLState $sqlState)"
}

private const val MAX_CAUSES = 8

fun Application.installErrors() {
    install(StatusPages) {
        exception<ApiException> { call, e -> call.respond(e.status, ApiError(e.message.orEmpty(), e.code)) }
        exception<SerializationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("Malformed request body", ErrorCode.BAD_REQUEST))
        }
        exception<PayloadTooLargeException> { call, _ ->
            call.respond(
                HttpStatusCode.PayloadTooLarge,
                ApiError("Image is larger than 5 MB", ErrorCode.PAYLOAD_TOO_LARGE)
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("Malformed request body", ErrorCode.BAD_REQUEST))
        }
        exception<Throwable> { call, e ->
            call.application.environment.log.error("Unhandled error: {}", describe(e))
            call.respond(HttpStatusCode.InternalServerError, ApiError("Internal error", ErrorCode.INTERNAL))
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound, ApiError("Not found", ErrorCode.NOT_FOUND))
        }
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(HttpStatusCode.Unauthorized, ApiError("Unauthorized", ErrorCode.UNAUTHORIZED))
        }
    }
}
