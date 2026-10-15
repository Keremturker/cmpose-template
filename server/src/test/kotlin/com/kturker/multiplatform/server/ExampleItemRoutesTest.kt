package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.db.SpaceRepository
import com.kturker.multiplatform.server.db.UserRepository
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The reference pattern in ExampleItemRoutes.kt keeps the role rules: viewers read, editors write, others see 404. */
class ExampleItemRoutesTest {
    @BeforeTest
    fun clean() {
        transaction(TestDb.database) { SchemaUtils.create(ExampleItems) }
        TestDb.reset() // the CASCADE truncate of spaces empties example_items too
    }

    private val owner = TestKeys.token("owner")
    private val editor = TestKeys.token("editor")
    private val viewer = TestKeys.token("viewer")
    private val stranger = TestKeys.token("stranger")

    private fun ApplicationTestBuilder.app() {
        application {
            module(TestKeys.dbDeps())
            routing {
                authenticate(FIREBASE_AUTH) {
                    install(touchUsers(UserRepository(TestDb.database)))
                    exampleItemRoutes(SpaceRepository(TestDb.database), TestDb.database)
                }
            }
        }
    }

    @Test
    fun `editors write, viewers read but cannot write, strangers see nothing`() = testApplication {
        app()
        val client = jsonClient()
        val space = client.createSpace(owner, "Trip")
        client.join(owner, space.id, Role.EDITOR, editor)
        client.join(owner, space.id, Role.VIEWER, viewer)
        client.spaces(stranger)
        val items = "/spaces/${space.id}/items"

        val created = client.post(items) { jsonBody(editor, ExampleItemDraft("Tickets")) }
        assertEquals(HttpStatusCode.Created, created.status)
        val item: ExampleItem = created.body()

        assertEquals(listOf(item), client.get(items) { bearerAuth(viewer) }.body<List<ExampleItem>>())
        assertEquals(HttpStatusCode.NotFound, client.post(items) { jsonBody(viewer, ExampleItemDraft("x")) }.status)
        assertEquals(HttpStatusCode.NotFound, client.get(items) { bearerAuth(stranger) }.status)
        assertEquals(HttpStatusCode.NotFound, client.post(items) { jsonBody(stranger, ExampleItemDraft("x")) }.status)
        assertEquals(1, client.get(items) { bearerAuth(owner) }.body<List<ExampleItem>>().size)
    }

    @Test
    fun `items stay in their space`() = testApplication {
        app()
        val client = jsonClient()
        val trip = client.createSpace(owner, "Trip")
        val home = client.createSpace(owner, "Home")
        client.post("/spaces/${trip.id}/items") { jsonBody(owner, ExampleItemDraft("Tickets")) }
        assertEquals(
            emptyList(),
            client.get("/spaces/${home.id}/items") { bearerAuth(owner) }.body<List<ExampleItem>>(),
        )
        assertEquals(HttpStatusCode.NotFound, client.get("/spaces/nope/items") { bearerAuth(owner) }.status)
    }
}
