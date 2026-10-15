package com.kturker.multiplatform.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.NetworkException
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.auth.principalOrThrow
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.KeyPairGenerator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * A refused token is logged at WARN with its reason, so a 401 on the device can be explained from the server's
 * log. The token itself, its uid and its email never appear in the log.
 */
class AuthRejectionLogTest {
    private val logger = LoggerFactory.getLogger("com.kturker.multiplatform.server.auth.FirebaseAuth") as Logger
    private val appender = ListAppender<ILoggingEvent>()

    private val uid = "secret-uid-42"
    private val email = "person@private.example"

    @BeforeTest fun attach() {
        appender.start()
        logger.addAppender(appender)
    }

    @AfterTest fun detach() {
        logger.detachAppender(appender)
    }

    private fun rejectionLogFor(token: String, jwkProvider: JwkProvider = TestKeys.jwkProvider): List<String> {
        testApplication {
            application {
                module(AppDeps(TestKeys.config(), jwkProvider, null))
                routing { authenticate(FIREBASE_AUTH) { get("/whoami") { call.respondText(call.principalOrThrow().uid) } } }
            }
            assertEquals(HttpStatusCode.Unauthorized, client.get("/whoami") { bearerAuth(token) }.status)
        }
        val events = appender.list.toList()
        events.forEach { event ->
            assertEquals(Level.WARN, event.level)
            listOf(token, uid, email).forEach { secret ->
                assertFalse(event.formattedMessage.contains(secret), "log leaks: ${event.formattedMessage}")
            }
        }
        return events.map { it.formattedMessage }
    }

    @Test
    fun `an expired token is logged as expired`() {
        assertEquals(
            listOf("Token rejected: expired"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email, expiresInSec = -120))
        )
    }

    @Test
    fun `a token for another project is logged as wrong audience`() {
        assertEquals(
            listOf("Token rejected: wrong audience"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email, audience = "someone-else"))
        )
    }

    @Test
    fun `a foreign issuer is logged as wrong issuer`() {
        assertEquals(
            listOf("Token rejected: wrong issuer"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email, issuer = "https://evil.example/x"))
        )
    }

    @Test
    fun `a forged signature is logged as bad signature`() {
        val stranger = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        assertEquals(
            listOf("Token rejected: bad signature"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email, signWith = stranger.private))
        )
    }

    @Test
    fun `an unpublished kid is logged as unknown kid`() {
        assertEquals(
            listOf("Token rejected: unknown kid"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email, kid = "gone"))
        )
    }

    @Test
    fun `a failed key fetch is logged as JWKS unavailable`() {
        val offline = object : JwkProvider {
            override fun get(keyId: String): Jwk = throw NetworkException("Cannot obtain jwks", IOException("down"))
        }
        assertEquals(
            listOf("Token rejected: JWKS unavailable"),
            rejectionLogFor(TestKeys.token(uid = uid, email = email), offline)
        )
    }

    @Test
    fun `a token without a subject is logged as no subject`() {
        assertEquals(listOf("Token rejected: no subject"), rejectionLogFor(TestKeys.token(uid = "", email = email)))
    }

    @Test
    fun `garbage is logged as malformed`() {
        assertEquals(listOf("Token rejected: malformed token"), rejectionLogFor("not-a-jwt"))
    }
}
