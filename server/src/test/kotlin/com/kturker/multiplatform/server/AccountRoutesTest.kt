package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.UploadResult
import com.kturker.multiplatform.server.db.Files
import com.kturker.multiplatform.server.db.Invites
import com.kturker.multiplatform.server.db.SpaceMembers
import com.kturker.multiplatform.server.db.Spaces
import com.kturker.multiplatform.server.db.Users
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountRoutesTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val alice = TestKeys.token("alice")
    private val bob = TestKeys.token("bob")

    private suspend fun HttpClient.uploadId(token: String, spaceId: String): String {
        val response = upload(token, spaceId, PNG_BYTES)
        assertEquals(HttpStatusCode.Created, response.status)
        return response.body<UploadResult>().fileId
    }

    private fun userExists(uid: String) = transaction(TestDb.database) {
        Users.selectAll().where { Users.uid eq uid }.count() > 0
    }

    @Test
    fun `deleting the owner's account removes owned spaces and their files but keeps spaces where they were only a member`() =
        testApplication {
            val dir = createTempDirectory("files").toFile()
            application { module(TestKeys.dbDeps(dir.path)) }
            val client = jsonClient()
            val aliceSpace = client.createSpace(alice, "Alice's")
            client.join(alice, aliceSpace.id, Role.EDITOR, bob)
            val bobSpace = client.createSpace(bob, "Bob's")
            client.join(bob, bobSpace.id, Role.EDITOR, alice)
            val alicePersonal = client.spaces(alice).single { it.isPersonal }
            // An invite Alice created that nobody used yet
            client.invite(alice, aliceSpace.id, Role.VIEWER)

            val inAliceSpace = client.uploadId(bob, aliceSpace.id)
            val inPersonal = client.uploadId(alice, alicePersonal.id)
            val aliceInBobSpace = client.uploadId(alice, bobSpace.id)
            val bobInBobSpace = client.uploadId(bob, bobSpace.id)

            assertEquals(HttpStatusCode.NoContent, client.delete("/me") { bearerAuth(alice) }.status)

            // Alice, her spaces and their files are gone, rows and disk alike
            assertFalse(userExists("alice"))
            assertFalse(dir.resolve(aliceSpace.id).exists())
            assertFalse(dir.resolve(alicePersonal.id).exists())
            assertEquals(HttpStatusCode.NotFound, client.get("/files/$inAliceSpace") { bearerAuth(bob) }.status)
            transaction(TestDb.database) {
                val gone = listOf(aliceSpace.id, alicePersonal.id).map(UUID::fromString)
                assertEquals(0L, Spaces.selectAll().where { Spaces.id inList gone }.count())
                assertEquals(0L, Files.selectAll().where { Files.spaceId inList gone }.count())
                assertEquals(0L, Invites.selectAll().where { Invites.createdBy eq "alice" }.count())
                assertEquals(0L, SpaceMembers.selectAll().where { SpaceMembers.uid eq "alice" }.count())
            }
            assertEquals(HttpStatusCode.NotFound, client.get("/spaces/${aliceSpace.id}/members") { bearerAuth(bob) }.status)

            // Bob's space stays, without Alice, with both files still there
            assertEquals(listOf("Personal", "Bob's"), client.spaces(bob).map { it.name })
            val members: List<Member> = client.get("/spaces/${bobSpace.id}/members") { bearerAuth(bob) }.body()
            assertEquals(listOf("bob"), members.map { it.uid })
            for (fileId in listOf(aliceInBobSpace, bobInBobSpace)) {
                assertEquals(HttpStatusCode.OK, client.get("/files/$fileId") { bearerAuth(bob) }.status, fileId)
                assertTrue(dir.resolve("${bobSpace.id}/$fileId").isFile, fileId)
            }
            // Only the file uploaded in Alice's personal space went with it (not some other folder)
            assertFalse(dir.walkTopDown().any { it.name == inPersonal })
        }

    // The app retries DELETE /me after Firebase asks for a fresh sign-in: the second call finds nothing to delete.
    @Test
    fun `deleting twice is 204`() = testApplication {
        val dir = createTempDirectory("files").toFile()
        application { module(TestKeys.dbDeps(dir.path)) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.uploadId(alice, space.id)

        assertEquals(HttpStatusCode.NoContent, client.delete("/me") { bearerAuth(alice) }.status)
        assertEquals(HttpStatusCode.NoContent, client.delete("/me") { bearerAuth(alice) }.status)
        assertFalse(userExists("alice"))
        assertFalse(dir.walkTopDown().any { it.isFile })
    }

    @Test
    fun `a later request with the same token starts from an empty account`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        client.createSpace(alice, "Trip")
        assertEquals(HttpStatusCode.NoContent, client.delete("/me") { bearerAuth(alice) }.status)
        assertEquals(listOf("Personal"), client.spaces(alice).map { it.name })
    }

    @Test
    fun `account deletion requires a token`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        assertEquals(HttpStatusCode.Unauthorized, client.delete("/me").status)
    }

    @Test
    fun `a file that cannot be removed from disk is logged by id and the account is still deleted`() {
        val dir = createTempDirectory("files").toFile()
        lateinit var fileId: String
        val warnings = capturedWarnings {
            testApplication {
                application { module(TestKeys.dbDeps(dir.path)) }
                val client = jsonClient()
                val space = client.createSpace(alice, "Trip")
                fileId = client.uploadId(alice, space.id)
                withReadOnlyDir(dir.resolve(space.id)) {
                    assertEquals(HttpStatusCode.NoContent, client.delete("/me") { bearerAuth(alice) }.status)
                }
            }
        }
        assertFalse(userExists("alice"))
        assertTrue(warnings.any { it.contains("Could not delete file $fileId") }, warnings.joinToString("\n"))
        assertTrue(warnings.any { it.contains("Could not delete a space's file folder") }, warnings.joinToString("\n"))
        warnings.forEach { assertFalse(it.contains("alice"), "uid in log: $it") }
    }
}
