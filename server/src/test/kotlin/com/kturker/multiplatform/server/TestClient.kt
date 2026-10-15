package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.InviteLink
import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.core.api.SpaceDraft
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.assertEquals

/** Default `json()` settings on purpose: a real client must not need special settings to read the server. */
fun ApplicationTestBuilder.jsonClient(): HttpClient = createClient {
    install(ContentNegotiation) { json() }
}

/** Bearer token plus a JSON body, the shape of every write request in these tests. */
inline fun <reified T : Any> HttpRequestBuilder.jsonBody(token: String, body: T) {
    bearerAuth(token)
    contentType(ContentType.Application.Json)
    setBody(body)
}

suspend fun HttpClient.spaces(token: String): List<Space> = get("/spaces") { bearerAuth(token) }.body()

suspend fun HttpClient.createSpace(token: String, name: String): Space {
    val response = post("/spaces") { jsonBody(token, SpaceDraft(name)) }
    assertEquals(HttpStatusCode.Created, response.status)
    return response.body()
}

suspend fun HttpClient.invite(token: String, spaceId: String, role: Role): InviteLink {
    val response = post("/spaces/$spaceId/invites") { jsonBody(token, InviteRequest(role)) }
    assertEquals(HttpStatusCode.Created, response.status)
    return response.body()
}

/** The owner invites [memberToken]'s user with [role] and they accept. */
suspend fun HttpClient.join(ownerToken: String, spaceId: String, role: Role, memberToken: String) {
    val link = invite(ownerToken, spaceId, role)
    assertEquals(HttpStatusCode.OK, post("/invites/${link.token}/accept") { bearerAuth(memberToken) }.status)
}

/** The smallest bytes [com.kturker.multiplatform.server.files.ImageSniffer] takes for a PNG, plus some payload. */
val PNG_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + ByteArray(100) { 7 }

/** Uploads [bytes] as a multipart file part to `/spaces/{spaceId}/files`; the part claims to be a PNG named [name]. */
suspend fun HttpClient.upload(token: String, spaceId: String, bytes: ByteArray, name: String = "a.png"): HttpResponse =
    submitFormWithBinaryData(
        url = "/spaces/$spaceId/files",
        formData = formData {
            append(
                "file",
                bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                },
            )
        },
    ) { bearerAuth(token) }
