package com.kturker.multiplatform.server

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.core.read.ListAppender
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.auth.principalOrThrow
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A database failure while recording the caller (the first thing every authenticated request does) answers 500 with
 * the shared body, and nothing the server logs — messages or exceptions, from any logger — carries the uid or email.
 * The embedded PostgreSQL's own server log (forwarded under `io.zonky`) is the database's log, not the app's, and
 * exists only in tests; it is left out.
 */
class DbErrorLogTest {
    private val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
    private val appender = ListAppender<ILoggingEvent>()

    private val uid = "secret-uid-42"
    private val email = "person@private.example"

    @BeforeTest fun attach() {
        appender.start()
        root.addAppender(appender)
    }

    @AfterTest fun detach() {
        root.detachAppender(appender)
    }

    @Test
    fun `a database error during an authenticated request logs neither uid nor email`() {
        testApplication {
            application {
                // module() wires touchUsers onto its own routes; the test route gets the same plugin explicitly
                module(AppDeps(TestKeys.config(), TestKeys.jwkProvider, TestDb.rejectingUsers))
                routing {
                    authenticate(FIREBASE_AUTH) {
                        install(touchUsers(com.kturker.multiplatform.server.db.UserRepository(TestDb.rejectingUsers)))
                        get("/whoami") { call.respondText(call.principalOrThrow().uid) }
                    }
                }
            }
            val response = client.get("/whoami") { bearerAuth(TestKeys.token(uid = uid, email = email)) }
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("""{"error":"Internal error","code":"internal"}""", response.bodyAsText())
        }
        val events = appender.list.filterNot { it.loggerName.startsWith("io.zonky") }
        assertTrue(events.any { it.formattedMessage.startsWith("Unhandled error") }, "the failure is still logged")
        events.forEach { event ->
            val text = event.formattedMessage + event.throwableProxy?.let(::render).orEmpty()
            listOf(uid, email).forEach { secret ->
                assertFalse(text.contains(secret), "log leaks [${event.loggerName}]: $text")
            }
        }
    }

    private fun render(proxy: IThrowableProxy): String = buildString {
        generateSequence(proxy) { it.cause }.forEach { append(it.className).append(": ").append(it.message) }
    }
}
