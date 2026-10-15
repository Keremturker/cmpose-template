package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.RoleChange
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val JOIN_GAP_MS = 5L

class MemberRoutesTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val alice = TestKeys.token("alice", name = "Alice")
    private val bob = TestKeys.token("bob")
    private val carol = TestKeys.token("carol")

    private suspend fun HttpClient.members(token: String, spaceId: String): List<Member> =
        get("/spaces/$spaceId/members") { bearerAuth(token) }.body()

    @Test
    fun `owner changes a role and removes a member`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.VIEWER, bob)

        val change = client.patch("/spaces/${space.id}/members/bob") { jsonBody(alice, RoleChange(Role.EDITOR)) }
        assertEquals(HttpStatusCode.NoContent, change.status)
        assertEquals(Role.EDITOR, client.members(alice, space.id).single { it.uid == "bob" }.role)
        assertEquals(Role.EDITOR, client.spaces(bob).single { it.id == space.id }.role)

        assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}/members/bob") { bearerAuth(alice) }.status)
        assertEquals(listOf("alice"), client.members(alice, space.id).map { it.uid })
        assertTrue(client.spaces(bob).none { it.id == space.id })
        // The removed member now sees nothing
        assertEquals(HttpStatusCode.NotFound, client.get("/spaces/${space.id}/members") { bearerAuth(bob) }.status)

        // Unknown members are 404 for both calls
        val ghost = client.patch("/spaces/${space.id}/members/bob") { jsonBody(alice, RoleChange(Role.EDITOR)) }
        assertEquals(HttpStatusCode.NotFound, ghost.status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/spaces/${space.id}/members/bob") { bearerAuth(alice) }.status)
    }

    @Test
    fun `a member can leave but the owner cannot`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.VIEWER, bob)
        client.join(alice, space.id, Role.EDITOR, carol)

        // An editor cannot remove someone else
        assertEquals(HttpStatusCode.NotFound, client.delete("/spaces/${space.id}/members/bob") { bearerAuth(carol) }.status)

        assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}/members/bob") { bearerAuth(bob) }.status)
        assertTrue(client.spaces(bob).none { it.id == space.id })

        val ownerLeaves = client.delete("/spaces/${space.id}/members/alice") { bearerAuth(alice) }
        assertEquals(HttpStatusCode.BadRequest, ownerLeaves.status)
        assertEquals(
            """{"error":"Owner cannot leave; delete the space","code":"bad_request"}""",
            ownerLeaves.bodyAsText(),
        )
        assertEquals(listOf("alice", "carol"), client.members(alice, space.id).map { it.uid })
    }

    @Test
    fun `owner cannot change own role or grant owner`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.VIEWER, bob)

        val own = client.patch("/spaces/${space.id}/members/alice") { jsonBody(alice, RoleChange(Role.VIEWER)) }
        assertEquals(HttpStatusCode.BadRequest, own.status)
        val grant = client.patch("/spaces/${space.id}/members/bob") { jsonBody(alice, RoleChange(Role.OWNER)) }
        assertEquals(HttpStatusCode.BadRequest, grant.status)
        assertTrue(grant.bodyAsText().contains("\"code\":\"bad_request\""))
        assertEquals(
            mapOf("alice" to Role.OWNER, "bob" to Role.VIEWER),
            client.members(alice, space.id).associate { it.uid to it.role },
        )
    }

    @Test
    fun `members list puts the owner first`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        // Bob and Carol exist before the space; Alice creates it after them
        client.spaces(bob)
        client.spaces(carol)
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.VIEWER, carol)
        Thread.sleep(JOIN_GAP_MS) // join times are in milliseconds; keep the two joins apart
        client.join(alice, space.id, Role.EDITOR, bob)

        val members = client.members(bob, space.id)
        assertEquals(listOf("alice", "carol", "bob"), members.map { it.uid })
        val owner = members.first()
        assertEquals(Role.OWNER, owner.role)
        assertEquals("Alice", owner.displayName)
        assertEquals("alice@example.com", owner.email)
        assertTrue(members.zipWithNext().all { (a, b) -> a.joinedAt <= b.joinedAt })
    }
}
