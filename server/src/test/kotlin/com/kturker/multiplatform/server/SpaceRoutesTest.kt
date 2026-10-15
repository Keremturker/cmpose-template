package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Profile
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.core.api.SpaceDraft
import com.kturker.multiplatform.core.api.UploadResult
import com.kturker.multiplatform.server.db.Files
import com.kturker.multiplatform.server.db.Invites
import com.kturker.multiplatform.server.db.SpaceMembers
import com.kturker.multiplatform.server.db.Spaces
import com.kturker.multiplatform.server.db.Users
import com.kturker.multiplatform.server.files.FileStore
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpaceRoutesTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val alice = TestKeys.token("alice")
    private val bob = TestKeys.token("bob")

    private fun displayName(uid: String) = transaction(TestDb.database) {
        Users.selectAll().where { Users.uid eq uid }.single()[Users.displayName]
    }

    @Test
    fun `list shows personal space first with role and member count`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        client.createSpace(alice, "Zoo")
        client.createSpace(alice, "apple")
        val beta = client.createSpace(bob, "Beta")
        client.join(bob, beta.id, Role.VIEWER, alice)

        val spaces = client.spaces(alice)
        assertEquals(listOf("Personal", "apple", "Beta", "Zoo"), spaces.map { it.name })
        val personal = spaces.first()
        assertTrue(personal.isPersonal)
        assertEquals(Role.OWNER, personal.role)
        assertEquals(1, personal.memberCount)
        val shared = spaces.single { it.name == "Beta" }
        assertEquals(Role.VIEWER, shared.role)
        assertEquals(2, shared.memberCount)
        assertFalse(shared.isPersonal)
    }

    @Test
    fun `create returns the new space with the creator as owner`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "  Trip  ")
        assertEquals(Space(space.id, "Trip", Role.OWNER, isPersonal = false, memberCount = 1), space)
        assertNotNull(UUID.fromString(space.id))

        val renamed = client.patch("/spaces/${space.id}") { jsonBody(alice, SpaceDraft(" Holiday ")) }
        assertEquals(HttpStatusCode.OK, renamed.status)
        assertEquals(space.copy(name = "Holiday"), renamed.body<Space>())
    }

    @Test
    fun `personal space cannot be deleted`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val personal = client.spaces(alice).single()
        val response = client.delete("/spaces/${personal.id}") { bearerAuth(alice) }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("""{"error":"Personal space cannot be deleted","code":"bad_request"}""", response.bodyAsText())
        assertEquals(listOf(personal.id), client.spaces(alice).map { it.id })
    }

    @Test
    fun `blank name is rejected and long names are rejected`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        for (name in listOf("", "   ", "x".repeat(81))) {
            val response = client.post("/spaces") { jsonBody(alice, SpaceDraft(name)) }
            assertEquals(HttpStatusCode.BadRequest, response.status, "'$name'")
            assertTrue(response.bodyAsText().contains("\"code\":\"bad_request\""))
        }
        val longest = client.createSpace(alice, "x".repeat(80))
        val blankRename = client.patch("/spaces/${longest.id}") { jsonBody(alice, SpaceDraft(" ")) }
        assertEquals(HttpStatusCode.BadRequest, blankRename.status)
        assertEquals(listOf("Personal", "x".repeat(80)), client.spaces(alice).map { it.name })
    }

    @Test
    fun `deleting a space removes its members invites and files`() = testApplication {
        val dir = createTempDirectory("files")
        application { module(TestKeys.dbDeps(dir.toString())) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val spaceId = UUID.fromString(space.id)
        client.join(alice, space.id, Role.EDITOR, bob)
        client.invite(alice, space.id, Role.VIEWER)
        val fileId = UUID.randomUUID()
        val path = FileStore(dir).write(space.id, fileId.toString(), byteArrayOf(1, 2, 3))
        transaction(TestDb.database) {
            Files.insert {
                it[id] = fileId
                it[Files.spaceId] = spaceId
                it[uploadedBy] = "bob"
                it[Files.path] = path
                it[mime] = "image/png"
                it[size] = 3L
                it[createdAt] = 0L
            }
        }
        assertTrue(dir.resolve(path).toFile().isFile)

        assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}") { bearerAuth(alice) }.status)

        transaction(TestDb.database) {
            assertEquals(0L, Spaces.selectAll().where { Spaces.id eq spaceId }.count())
            assertEquals(0L, SpaceMembers.selectAll().where { SpaceMembers.spaceId eq spaceId }.count())
            assertEquals(0L, Invites.selectAll().count())
            assertEquals(0L, Files.selectAll().count())
        }
        assertFalse(dir.resolve(path).toFile().exists(), "file removed from disk")
        assertEquals(listOf("Personal"), client.spaces(alice).map { it.name })
        assertEquals(listOf("Personal"), client.spaces(bob).map { it.name })
        assertEquals(HttpStatusCode.NotFound, client.get("/spaces/${space.id}/members") { bearerAuth(alice) }.status)
    }

    @Test
    fun `profile renames the personal space and display name`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val other = client.createSpace(alice, "Trip")
        val response = client.put("/me/profile") { jsonBody(alice, Profile("Alice A", "  Kişisel  ")) }
        assertEquals(HttpStatusCode.NoContent, response.status)

        assertEquals(listOf("Kişisel", "Trip"), client.spaces(alice).map { it.name })
        assertEquals("Alice A", displayName("alice"))
        assertEquals("Trip", client.spaces(alice).single { it.id == other.id }.name)

        // Without a display name the stored one stays; the personal space is no longer the default, so it stays too
        client.put("/me/profile") { jsonBody(alice, Profile(personalSpaceName = "Mine")) }
        assertEquals("Kişisel", client.spaces(alice).first().name)
        assertEquals("Alice A", displayName("alice"))

        for (bad in listOf(Profile("Alice", " "), Profile(" ", "Mine"), Profile("Alice", "x".repeat(81)))) {
            assertEquals(HttpStatusCode.BadRequest, client.put("/me/profile") { jsonBody(alice, bad) }.status, "$bad")
        }
    }

    @Test
    fun `a personal space the owner renamed keeps its name through later profile syncs`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        // A fresh user: the first sync localizes the default name
        client.put("/me/profile") { jsonBody(alice, Profile("Alice", "Kişisel")) }
        val personal = client.spaces(alice).single { it.isPersonal }
        assertEquals("Kişisel", personal.name)

        val renamed = client.patch("/spaces/${personal.id}") { jsonBody(alice, SpaceDraft("Home")) }
        assertEquals(HttpStatusCode.OK, renamed.status)
        // Every sign-in sends the profile again
        val resync = client.put("/me/profile") { jsonBody(alice, Profile("Alice", "Kişisel")) }
        assertEquals(HttpStatusCode.NoContent, resync.status)
        assertEquals("Home", client.spaces(alice).single { it.isPersonal }.name)

        // Another fresh user still gets the localized name
        client.put("/me/profile") { jsonBody(bob, Profile("Bob", "Personnel")) }
        assertEquals("Personnel", client.spaces(bob).single { it.isPersonal }.name)
    }

    @Test
    fun `a display name set through the profile survives later logins`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        client.put("/me/profile") { jsonBody(TestKeys.token("alice", name = "Token Name"), Profile("Chosen", "Mine")) }
        assertEquals("Chosen", displayName("alice"))

        client.spaces(TestKeys.token("alice", name = "Another Token Name"))
        assertEquals("Chosen", displayName("alice"))
    }

    @Test
    fun `profile requires a token`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        assertEquals(HttpStatusCode.Unauthorized, client.put("/me/profile").status)
    }

    @Test
    fun `a space whose files cannot be removed from disk is still deleted and the failure is logged`() {
        val dir = createTempDirectory("files").toFile()
        lateinit var fileId: String
        val warnings = capturedWarnings {
            testApplication {
                application { module(TestKeys.dbDeps(dir.path)) }
                val client = jsonClient()
                val space = client.createSpace(alice, "Trip")
                fileId = client.upload(alice, space.id, PNG_BYTES).body<UploadResult>().fileId
                withReadOnlyDir(dir.resolve(space.id)) {
                    assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}") { bearerAuth(alice) }.status)
                }
                assertEquals(listOf("Personal"), client.spaces(alice).map { it.name })
            }
        }
        assertTrue(warnings.any { it.contains("Could not delete file $fileId") }, warnings.joinToString("\n"))
        assertTrue(warnings.any { it.contains("Could not delete a space's file folder") }, warnings.joinToString("\n"))
        warnings.forEach { assertFalse(it.contains("alice"), "uid in log: $it") }
    }
}
