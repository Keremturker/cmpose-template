package com.kturker.multiplatform.server

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.kturker.multiplatform.core.api.Role
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Invite codes and uids travel in request paths (`/invites/{token}`, `/spaces/{id}/members/{uid}`). The request log
 * shows those segments as placeholders, so a log line can never be replayed as an invite or name a user.
 */
class CallLogRedactionTest {
    private val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
    private val appender = ListAppender<ILoggingEvent>()

    @BeforeTest
    fun attach() {
        TestDb.reset()
        appender.start()
        root.addAppender(appender)
    }

    @AfterTest
    fun detach() {
        root.detachAppender(appender)
    }

    @Test
    fun `invite codes and member uids never reach the request log`() {
        val owner = TestKeys.token("owner")
        val memberUid = "secret-uid-42"
        val member = TestKeys.token(memberUid)
        lateinit var token: String
        testApplication {
            application { module(TestKeys.dbDeps()) }
            val client = jsonClient()
            val space = client.createSpace(owner, "Trip")
            token = client.invite(owner, space.id, Role.VIEWER).token
            assertEquals(HttpStatusCode.OK, client.get("/invites/$token") { bearerAuth(member) }.status)
            assertEquals(HttpStatusCode.OK, client.post("/invites/$token/accept") { bearerAuth(member) }.status)
            client.delete("/spaces/${space.id}/members/$memberUid") { bearerAuth(owner) }
            client.get("/invites/$token?x=1") { bearerAuth(member) }
        }
        val lines = appender.list.map { it.formattedMessage }
        lines.forEach { line ->
            assertFalse(line.contains(token), "invite code in log: $line")
            assertFalse(line.contains(memberUid), "uid in log: $line")
        }
        assertTrue(lines.any { it.contains("GET - /invites/{token}") }, lines.joinToString("\n"))
        assertTrue(lines.any { it.contains("POST - /invites/{token}/accept") }, lines.joinToString("\n"))
        assertTrue(lines.any { it.contains("/members/{uid}") }, lines.joinToString("\n"))
    }

    @Test
    fun `redaction keeps other paths as they are`() {
        assertEquals("/spaces/abc/members", redactedPath("/spaces/abc/members"))
        assertEquals("/spaces/abc/members/{uid}", redactedPath("/spaces/abc/members/u1"))
        assertEquals("/invites/{token}", redactedPath("/invites/t1"))
        assertEquals("/invites/{token}/accept", redactedPath("/invites/t1/accept"))
        assertEquals("/invites", redactedPath("/invites"))
        assertEquals("/health", redactedPath("/health"))
    }
}
