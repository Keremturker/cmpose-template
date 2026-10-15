package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.InvitePreview
import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.InviteState
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.server.db.AcceptResult
import com.kturker.multiplatform.server.db.INVITE_TTL_MS
import com.kturker.multiplatform.server.db.InviteRepository
import com.kturker.multiplatform.server.db.Invites
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InviteRoutesTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val alice = TestKeys.token("alice", name = "Alice")
    private val bob = TestKeys.token("bob")
    private val carol = TestKeys.token("carol")

    private fun sha256Hex(text: String) =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

    @Test
    fun `invite is single use`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val link = client.invite(alice, space.id, Role.EDITOR)

        val preview: InvitePreview = client.get("/invites/${link.token}") { bearerAuth(bob) }.body()
        assertEquals(InvitePreview("Trip", "Alice", Role.EDITOR, InviteState.VALID), preview)

        val joined = client.post("/invites/${link.token}/accept") { bearerAuth(bob) }
        assertEquals(HttpStatusCode.OK, joined.status)
        val space2: Space = joined.body()
        assertEquals(space.id, space2.id)
        assertEquals(Role.EDITOR, space2.role)
        assertEquals(2, space2.memberCount)

        val again = client.post("/invites/${link.token}/accept") { bearerAuth(carol) }
        assertEquals(HttpStatusCode.Conflict, again.status)
        assertEquals("""{"error":"Invite already used","code":"conflict"}""", again.bodyAsText())
        val used: InvitePreview = client.get("/invites/${link.token}") { bearerAuth(carol) }.body()
        assertEquals(InviteState.USED, used.state)
        assertTrue(client.spaces(carol).none { it.id == space.id })
    }

    @Test
    fun `invite expires after seven days`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val spaceId = UUID.fromString(space.id)

        // An invite made just over seven days ago, as the real clock sees it now
        val made = System.currentTimeMillis() - INVITE_TTL_MS - 1
        val link = InviteRepository(TestDb.database, now = { made }).create(spaceId, "alice", Role.VIEWER)
        assertEquals(made + INVITE_TTL_MS, link.expiresAt)

        val preview: InvitePreview = client.get("/invites/${link.token}") { bearerAuth(bob) }.body()
        assertEquals(InviteState.EXPIRED, preview.state)
        val accept = client.post("/invites/${link.token}/accept") { bearerAuth(bob) }
        assertEquals(HttpStatusCode.Gone, accept.status)
        assertEquals("""{"error":"Invite expired","code":"expired"}""", accept.bodyAsText())
        assertTrue(client.spaces(bob).none { it.id == space.id })
    }

    @Test
    fun `an invite is valid until its last millisecond`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.spaces(bob)
        var clock = 1_000L
        val invites = InviteRepository(TestDb.database, now = { clock })
        val link = invites.create(UUID.fromString(space.id), "alice", Role.VIEWER)

        clock += INVITE_TTL_MS - 1
        assertEquals(InviteState.VALID, invites.preview(link.token)?.first?.state)
        clock += 1
        assertEquals(InviteState.EXPIRED, invites.preview(link.token)?.first?.state)
        assertEquals(AcceptResult.Expired, invites.accept(link.token, "bob"))
    }

    @Test
    fun `accepting while already a member keeps the role and does not use the invite`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.EDITOR, bob)

        val link = client.invite(alice, space.id, Role.VIEWER)
        val again = client.post("/invites/${link.token}/accept") { bearerAuth(bob) }
        assertEquals(HttpStatusCode.OK, again.status)
        assertEquals(Role.EDITOR, again.body<Space>().role)
        val preview: InvitePreview = client.get("/invites/${link.token}") { bearerAuth(bob) }.body()
        assertEquals(InviteState.VALID, preview.state)

        // The owner's own accept is a no-op too
        assertEquals(Role.OWNER, client.post("/invites/${link.token}/accept") { bearerAuth(alice) }.body<Space>().role)

        // Still good for someone new
        val carolJoin = client.post("/invites/${link.token}/accept") { bearerAuth(carol) }
        assertEquals(Role.VIEWER, carolJoin.body<Space>().role)
    }

    @Test
    fun `token is never stored in clear`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val link = client.invite(alice, space.id, Role.VIEWER)

        val hash = transaction(TestDb.database) { Invites.selectAll().single()[Invites.tokenHash] }
        assertNotEquals(link.token, hash)
        assertEquals(sha256Hex(link.token), hash)
        // 32 random bytes, URL-safe base64 without padding
        assertEquals(32, Base64.getUrlDecoder().decode(link.token).size)
        assertTrue(link.token.all { it.isLetterOrDigit() || it == '-' || it == '_' }, link.token)
        assertNotEquals(link.token, client.invite(alice, space.id, Role.VIEWER).token)
    }

    @Test
    fun `unknown token preview is 404`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val preview = client.get("/invites/no-such-token") { bearerAuth(bob) }
        assertEquals(HttpStatusCode.NotFound, preview.status)
        assertEquals("""{"error":"Not found","code":"not_found"}""", preview.bodyAsText())
        assertEquals(HttpStatusCode.NotFound, client.post("/invites/no-such-token/accept") { bearerAuth(bob) }.status)
    }

    @Test
    fun `owner role cannot be invited`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val response = client.post("/spaces/${space.id}/invites") { jsonBody(alice, InviteRequest(Role.OWNER)) }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("\"code\":\"bad_request\""))
        assertEquals(0L, transaction(TestDb.database) { Invites.selectAll().count() })
    }

    @Test
    fun `invite expiry is seven days from creation`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val before = System.currentTimeMillis()
        val link = client.invite(alice, space.id, Role.VIEWER)
        val after = System.currentTimeMillis()
        assertTrue(link.expiresAt in (before + INVITE_TTL_MS)..(after + INVITE_TTL_MS))
        assertEquals(7L * 24 * 60 * 60 * 1000, INVITE_TTL_MS)
    }

    @Test
    fun `concurrent accepts use an invite once`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val users = (1..8).map { "racer$it" }
        users.forEach { client.spaces(TestKeys.token(it)) }
        val invites = InviteRepository(TestDb.database)
        val link = invites.create(UUID.fromString(space.id), "alice", Role.VIEWER)

        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(users.size)
        val results = try {
            users.map { uid -> pool.submit<AcceptResult> { start.await(); invites.accept(link.token, uid) } }
                .also { start.countDown() }
                .map { it.get(30, TimeUnit.SECONDS) }
        } finally {
            pool.shutdownNow()
        }
        assertEquals(1, results.count { it is AcceptResult.Joined }, "$results")
        assertEquals(users.size - 1, results.count { it == AcceptResult.Used }, "$results")
        assertEquals(2, client.spaces(alice).single { it.id == space.id }.memberCount)
    }

    @Test
    fun `deleteCreatedBy removes the user's invites only`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val aliceSpace = client.createSpace(alice, "Trip")
        val bobSpace = client.createSpace(bob, "Home")
        val aliceLink = client.invite(alice, aliceSpace.id, Role.VIEWER)
        val bobLink = client.invite(bob, bobSpace.id, Role.VIEWER)

        val invites = InviteRepository(TestDb.database)
        invites.deleteCreatedBy("alice")
        assertEquals(null, invites.preview(aliceLink.token))
        assertIs<AcceptResult.Joined>(invites.accept(bobLink.token, "alice"))
    }
}
