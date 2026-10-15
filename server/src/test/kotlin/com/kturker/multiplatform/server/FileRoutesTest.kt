package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.ApiError
import com.kturker.multiplatform.core.api.ErrorCode
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.UploadResult
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileRoutesTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    private val alice = TestKeys.token("alice")
    private val bob = TestKeys.token("bob")
    private val carol = TestKeys.token("carol")

    @Test
    fun `upload then download returns the same bytes`() = testApplication {
        val dir = createTempDirectory("files").toFile()
        application { module(TestKeys.dbDeps(dir.path)) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val response = client.upload(alice, space.id, PNG_BYTES)
        assertEquals(HttpStatusCode.Created, response.status)
        val fileId = response.body<UploadResult>().fileId
        assertTrue(dir.resolve("${space.id}/$fileId").isFile, "stored under files/<spaceId>/<fileId>")

        val download = client.get("/files/$fileId") { bearerAuth(alice) }
        assertEquals(HttpStatusCode.OK, download.status)
        assertEquals("image/png", download.headers[HttpHeaders.ContentType])
        assertEquals("nosniff", download.headers["X-Content-Type-Options"])
        assertContentEquals(PNG_BYTES, download.bodyAsBytes())
    }

    @Test
    fun `an editor uploads and deletes a file`() = testApplication {
        val dir = createTempDirectory("files").toFile()
        application { module(TestKeys.dbDeps(dir.path)) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.EDITOR, bob)
        val response = client.upload(bob, space.id, PNG_BYTES)
        assertEquals(HttpStatusCode.Created, response.status)
        val fileId = response.body<UploadResult>().fileId

        assertEquals(HttpStatusCode.NoContent, client.delete("/files/$fileId") { bearerAuth(bob) }.status)
        assertEquals(HttpStatusCode.NotFound, client.get("/files/$fileId") { bearerAuth(alice) }.status)
        assertFalse(dir.walkTopDown().any { it.isFile }, "file removed from disk")
        assertEquals(HttpStatusCode.NotFound, client.delete("/files/$fileId") { bearerAuth(bob) }.status)
    }

    @Test
    fun `a viewer can download but not upload or delete`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.join(alice, space.id, Role.VIEWER, bob)
        val fileId = client.upload(alice, space.id, PNG_BYTES).body<UploadResult>().fileId

        val download = client.get("/files/$fileId") { bearerAuth(bob) }
        assertEquals(HttpStatusCode.OK, download.status)
        assertContentEquals(PNG_BYTES, download.bodyAsBytes())

        val upload = client.upload(bob, space.id, PNG_BYTES)
        assertEquals(HttpStatusCode.NotFound, upload.status)
        assertEquals(ErrorCode.NOT_FOUND, upload.body<ApiError>().code)
        assertEquals(HttpStatusCode.NotFound, client.delete("/files/$fileId") { bearerAuth(bob) }.status)
        assertEquals(HttpStatusCode.OK, client.get("/files/$fileId") { bearerAuth(alice) }.status)
    }

    @Test
    fun `a file in another space is 404`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        // Carol is a member elsewhere (her own space) but not in Alice's
        val other = client.createSpace(carol, "Other")
        client.join(alice, space.id, Role.EDITOR, bob)
        val fileId = client.upload(alice, space.id, PNG_BYTES).body<UploadResult>().fileId
        client.upload(carol, other.id, PNG_BYTES).also { assertEquals(HttpStatusCode.Created, it.status) }

        val download = client.get("/files/$fileId") { bearerAuth(carol) }
        assertEquals(HttpStatusCode.NotFound, download.status)
        assertEquals(ErrorCode.NOT_FOUND, download.body<ApiError>().code)
        assertEquals(HttpStatusCode.NotFound, client.delete("/files/$fileId") { bearerAuth(carol) }.status)
        assertEquals(HttpStatusCode.NotFound, client.upload(carol, space.id, PNG_BYTES).status)
        // Bob is an editor of Alice's space, not of Carol's
        assertEquals(HttpStatusCode.NotFound, client.upload(bob, other.id, PNG_BYTES).status)
        // The file is still there for its own space
        assertEquals(HttpStatusCode.OK, client.get("/files/$fileId") { bearerAuth(bob) }.status)
    }

    @Test
    fun `unknown and malformed file ids are 404`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        for (id in listOf(UUID.randomUUID().toString(), "not-a-uuid")) {
            assertEquals(HttpStatusCode.NotFound, client.get("/files/$id") { bearerAuth(alice) }.status, id)
            assertEquals(HttpStatusCode.NotFound, client.delete("/files/$id") { bearerAuth(alice) }.status, id)
        }
        assertEquals(HttpStatusCode.NotFound, client.upload(alice, "not-a-uuid", PNG_BYTES).status)
    }

    @Test
    fun `a pdf named like a jpeg is refused`() = testApplication {
        val dir = createTempDirectory("files").toFile()
        application { module(TestKeys.dbDeps(dir.path)) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val response = client.upload(alice, space.id, "%PDF-1.7 fake".toByteArray(), "x.jpg")
        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        assertEquals(ErrorCode.UNSUPPORTED_MEDIA_TYPE, response.body<ApiError>().code)
        assertFalse(dir.walkTopDown().any { it.isFile })
    }

    @Test
    fun `more than five megabytes is refused`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val response = client.upload(alice, space.id, PNG_BYTES + ByteArray(5 * 1024 * 1024))
        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
        assertEquals(ErrorCode.PAYLOAD_TOO_LARGE, response.body<ApiError>().code)
    }

    @Test
    fun `a body far over the limit still gets a json 413`() = testApplication {
        application { module(TestKeys.dbDeps()) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        val response = client.upload(alice, space.id, PNG_BYTES + ByteArray(12 * 1024 * 1024))
        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
        assertEquals(ErrorCode.PAYLOAD_TOO_LARGE, response.body<ApiError>().code)
    }

    @Test
    fun `deleting a space removes its files and folder from disk`() = testApplication {
        val dir = createTempDirectory("files").toFile()
        application { module(TestKeys.dbDeps(dir.path)) }
        val client = jsonClient()
        val space = client.createSpace(alice, "Trip")
        client.upload(alice, space.id, PNG_BYTES)
        client.upload(alice, space.id, PNG_BYTES)
        assertEquals(2, dir.walkTopDown().count { it.isFile })

        assertEquals(HttpStatusCode.NoContent, client.delete("/spaces/${space.id}") { bearerAuth(alice) }.status)
        assertFalse(dir.resolve(space.id).exists())
    }

    @Test
    fun `a file that cannot be removed from disk is logged by id and the delete still succeeds`() {
        val dir = createTempDirectory("files").toFile()
        lateinit var fileId: String
        val warnings = capturedWarnings {
            testApplication {
                application { module(TestKeys.dbDeps(dir.path)) }
                val client = jsonClient()
                val space = client.createSpace(alice, "Trip")
                fileId = client.upload(alice, space.id, PNG_BYTES).body<UploadResult>().fileId
                withReadOnlyDir(dir.resolve(space.id)) {
                    assertEquals(HttpStatusCode.NoContent, client.delete("/files/$fileId") { bearerAuth(alice) }.status)
                }
                assertEquals(HttpStatusCode.NotFound, client.get("/files/$fileId") { bearerAuth(alice) }.status)
            }
        }
        assertTrue(warnings.any { it.contains("Could not delete file $fileId") }, warnings.joinToString("\n"))
        warnings.forEach { assertFalse(it.contains("alice"), "uid in log: $it") }
    }
}
