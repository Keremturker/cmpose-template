package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.server.auth.FIREBASE_AUTH
import com.kturker.multiplatform.server.auth.principalOrThrow
import com.kturker.multiplatform.server.db.SpaceMembers
import com.kturker.multiplatform.server.db.Spaces
import com.kturker.multiplatform.server.db.UserRepository
import com.kturker.multiplatform.server.db.Users
import com.kturker.multiplatform.server.db.roleOf
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The first authenticated request creates the caller's user row and personal space. Until the space routes exist, a
 * test-only `/whoami` route with the same [touchUsers] plugin stands in for them.
 */
class UserBootstrapTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private fun ApplicationTestBuilder.app() {
        application {
            module(TestKeys.dbDeps())
            routing {
                authenticate(FIREBASE_AUTH) {
                    install(touchUsers(UserRepository(TestDb.database)))
                    get("/whoami") { call.respondText(call.principalOrThrow().uid) }
                }
            }
        }
    }

    private fun personalSpaces(uid: String) = transaction(TestDb.database) {
        Spaces.selectAll().where { Spaces.ownerUid eq uid }.filter { it[Spaces.isPersonal] }
    }

    @Test
    fun `the first request creates the user and a full personal space`() = testApplication {
        app()
        assertEquals(HttpStatusCode.OK, client.get("/whoami") { bearerAuth(TestKeys.token("alice")) }.status)

        transaction(TestDb.database) {
            val user = Users.selectAll().where { Users.uid eq "alice" }.single()
            assertEquals("alice@example.com", user[Users.email])
        }
        val space = personalSpaces("alice").single()
        assertEquals("Personal", space[Spaces.name])
        transaction(TestDb.database) {
            val members = SpaceMembers.selectAll().where { SpaceMembers.spaceId eq space[Spaces.id] }.toList()
            assertEquals(listOf("alice"), members.map { it[SpaceMembers.uid] })
            assertEquals(Role.OWNER, roleOf(members.single()[SpaceMembers.role]))
        }
    }

    @Test
    fun `a second request does not create a second personal space`() = testApplication {
        app()
        repeat(2) { client.get("/whoami") { bearerAuth(TestKeys.token("alice")) } }

        assertEquals(1, personalSpaces("alice").size)
        transaction(TestDb.database) {
            assertEquals(1L, Spaces.selectAll().count())
            assertEquals(1L, SpaceMembers.selectAll().count())
        }
    }

    @Test
    fun `the name claim fills an empty display name but never replaces one`() = testApplication {
        app()
        client.get("/whoami") { bearerAuth(TestKeys.token("alice")) }
        assertEquals(null, displayName("alice"))
        client.get("/whoami") { bearerAuth(TestKeys.token("alice", name = "Alice")) }
        assertEquals("Alice", displayName("alice"))

        // A stored name may have been chosen in the app (PUT /me/profile); the token must not overwrite it
        client.get("/whoami") { bearerAuth(TestKeys.token("alice", email = "new@example.com", name = "Alice B")) }
        assertEquals("Alice", displayName("alice"))
        transaction(TestDb.database) {
            assertEquals("new@example.com", Users.selectAll().where { Users.uid eq "alice" }.single()[Users.email])
        }
    }

    @Test
    fun `a token without a name keeps the stored display name`() = testApplication {
        app()
        client.get("/whoami") { bearerAuth(TestKeys.token("alice", name = "Alice")) }
        client.get("/whoami") { bearerAuth(TestKeys.token("alice")) }
        assertEquals("Alice", displayName("alice"))
    }

    @Test
    fun `each user gets their own personal space`() = testApplication {
        app()
        client.get("/whoami") { bearerAuth(TestKeys.token("alice")) }
        client.get("/whoami") { bearerAuth(TestKeys.token("bob")) }
        assertEquals(1, personalSpaces("alice").size)
        assertEquals(1, personalSpaces("bob").size)
        assertTrue(personalSpaces("alice").single()[Spaces.id] != personalSpaces("bob").single()[Spaces.id])
    }

    private fun displayName(uid: String) = transaction(TestDb.database) {
        Users.selectAll().where { Users.uid eq uid }.single()[Users.displayName]
    }
}
