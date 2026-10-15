package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.RoleChange
import com.kturker.multiplatform.core.api.SpaceDraft
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The role matrix of the space endpoints. A caller who may not do something gets 404 (never 403), exactly as if the
 * space did not exist, so a space id that leaks reveals nothing.
 */
class AccessTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val owner = TestKeys.token("owner")
    private val editor = TestKeys.token("editor")
    private val viewer = TestKeys.token("viewer")
    private val stranger = TestKeys.token("stranger")

    /** Every owner-only call on [spaceId], made as [token]. */
    private fun ownerOnlyCalls(client: HttpClient, spaceId: String, token: String) =
        listOf<Pair<String, suspend () -> HttpResponse>>(
            "rename" to { client.patch("/spaces/$spaceId") { jsonBody(token, SpaceDraft("x")) } },
            "delete" to { client.delete("/spaces/$spaceId") { bearerAuth(token) } },
            "invite" to { client.post("/spaces/$spaceId/invites") { jsonBody(token, InviteRequest(Role.VIEWER)) } },
            "change role" to {
                client.patch("/spaces/$spaceId/members/owner") { jsonBody(token, RoleChange(Role.VIEWER)) }
            },
            "remove owner" to { client.delete("/spaces/$spaceId/members/owner") { bearerAuth(token) } },
        )

    private suspend fun assertAllNotFound(calls: List<Pair<String, suspend () -> HttpResponse>>) {
        for ((name, call) in calls) {
            val response = call()
            assertEquals(HttpStatusCode.NotFound, response.status, name)
            assertEquals("""{"error":"Not found","code":"not_found"}""", response.bodyAsText(), name)
        }
    }

    @Test
    fun `a non member gets 404 on every space endpoint`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(owner, "Trip")
        client.get("/spaces") { bearerAuth(stranger) } // bootstraps the stranger

        val memberCalls = listOf<Pair<String, suspend () -> HttpResponse>>(
            "members" to { client.get("/spaces/${space.id}/members") { bearerAuth(stranger) } },
            "leave" to { client.delete("/spaces/${space.id}/members/stranger") { bearerAuth(stranger) } },
        )
        assertAllNotFound(ownerOnlyCalls(client, space.id, stranger) + memberCalls)
        assertEquals(listOf(false), client.spaces(stranger).map { it.id == space.id })
    }

    @Test
    fun `a viewer cannot invite, rename or delete`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(owner, "Trip")
        client.join(owner, space.id, Role.VIEWER, viewer)

        assertAllNotFound(ownerOnlyCalls(client, space.id, viewer))
        assertEquals(HttpStatusCode.OK, client.get("/spaces/${space.id}/members") { bearerAuth(viewer) }.status)
        assertEquals("Trip", client.spaces(owner).single { it.id == space.id }.name)
    }

    @Test
    fun `an editor cannot invite`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(owner, "Trip")
        client.join(owner, space.id, Role.EDITOR, editor)

        assertAllNotFound(ownerOnlyCalls(client, space.id, editor))
        assertEquals(HttpStatusCode.OK, client.get("/spaces/${space.id}/members") { bearerAuth(editor) }.status)
    }

    @Test
    fun `the owner may do everything`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(owner, "Trip")
        assertEquals(HttpStatusCode.OK, client.get("/spaces/${space.id}/members") { bearerAuth(owner) }.status)
        val renamed = client.patch("/spaces/${space.id}") { jsonBody(owner, SpaceDraft("Holiday")) }
        assertEquals(HttpStatusCode.OK, renamed.status)
        client.invite(owner, space.id, Role.VIEWER)
        assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}") { bearerAuth(owner) }.status)
    }

    @Test
    fun `a malformed space id is 404`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        for (path in listOf("/spaces/not-a-uuid/members", "/spaces/00000000-0000-0000-0000-000000000000/members")) {
            val response = client.get(path) { bearerAuth(owner) }
            assertEquals(HttpStatusCode.NotFound, response.status, path)
            assertEquals("""{"error":"Not found","code":"not_found"}""", response.bodyAsText(), path)
        }
        val patch = client.patch("/spaces/not-a-uuid") { jsonBody(owner, SpaceDraft("x")) }
        assertEquals(HttpStatusCode.NotFound, patch.status)
    }

    @Test
    fun `space endpoints need a token`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        for (path in listOf("/spaces", "/invites/abc")) {
            assertEquals(HttpStatusCode.Unauthorized, client.get(path).status, path)
        }
        assertEquals(HttpStatusCode.Unauthorized, client.post("/invites/abc/accept").status)
    }
}
