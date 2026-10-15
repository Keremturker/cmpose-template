package com.kturker.multiplatform.feature.sharing.presentation

import com.kturker.multiplatform.core.api.InviteLink
import com.kturker.multiplatform.core.api.InvitePreview
import com.kturker.multiplatform.core.api.InviteRequest
import com.kturker.multiplatform.core.api.InviteState
import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.RoleChange
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.core.api.SpaceDraft
import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.core.domain.RestResult
import com.kturker.multiplatform.core.network.TokenSource
import com.kturker.multiplatform.core.network.buildBackendClient
import com.kturker.multiplatform.feature.sharing.presentation.data.KtorSpacesRepository
import com.kturker.multiplatform.feature.sharing.presentation.data.SpacesRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SpacesRepositoryTest {

    private object Tokens : TokenSource {
        override suspend fun token(forceRefresh: Boolean): String? = "t"
    }

    private val requests = mutableListOf<HttpRequestData>()

    // A base URL with a path prefix: a path starting with "/" would escape it, so every call must stay under it
    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): SpacesRepository {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        return KtorSpacesRepository(buildBackendClient(engine, BASE, Tokens, isDebug = false))
    }

    // On real time: runTest's virtual clock would fire HttpTimeout before MockEngine answers on another dispatcher
    private fun apiTest(block: suspend CoroutineScope.() -> Unit): TestResult =
        runTest { withContext(Dispatchers.Default) { block() } }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun MockRequestHandleScope.noContent() = respond("", HttpStatusCode.NoContent)

    private suspend inline fun <reified T> HttpRequestData.decodedBody(): T =
        Json.decodeFromString(body.toByteArray().decodeToString())

    private fun lastCall() = requests.last().let { it.method to it.url.toString() }

    @Test
    fun everyCallStaysUnderTheBaseUrlPrefix() = apiTest {
        val spaceJson = """{"id":"s1","name":"Home","role":"owner","isPersonal":false,"memberCount":2}"""

        repository { json("[$spaceJson]") }.list()
        assertEquals(HttpMethod.Get to "${BASE}spaces", lastCall())

        repository { json(spaceJson, HttpStatusCode.Created) }.create("Home")
        assertEquals(HttpMethod.Post to "${BASE}spaces", lastCall())

        repository { json(spaceJson) }.rename("s1", "Home")
        assertEquals(HttpMethod.Patch to "${BASE}spaces/s1", lastCall())

        repository { noContent() }.delete("s1")
        assertEquals(HttpMethod.Delete to "${BASE}spaces/s1", lastCall())

        repository { json("[]") }.members("s1")
        assertEquals(HttpMethod.Get to "${BASE}spaces/s1/members", lastCall())

        repository { noContent() }.setRole("s1", "u2", Role.VIEWER)
        assertEquals(HttpMethod.Patch to "${BASE}spaces/s1/members/u2", lastCall())

        repository { noContent() }.remove("s1", "u2")
        assertEquals(HttpMethod.Delete to "${BASE}spaces/s1/members/u2", lastCall())

        repository { json("""{"token":"tok","expiresAt":5}""", HttpStatusCode.Created) }.invite("s1", Role.EDITOR)
        assertEquals(HttpMethod.Post to "${BASE}spaces/s1/invites", lastCall())

        repository {
            json("""{"spaceName":"Home","inviterName":null,"role":"editor","state":"valid"}""")
        }.preview("tok")
        assertEquals(HttpMethod.Get to "${BASE}invites/tok", lastCall())

        repository { json(spaceJson) }.accept("tok")
        assertEquals(HttpMethod.Post to "${BASE}invites/tok/accept", lastCall())
    }

    @Test
    fun bodiesAndAnswersAreTheSharedModels() = apiTest {
        val created = repository {
            json("""{"id":"s1","name":"Home","role":"owner","isPersonal":false,"memberCount":1}""")
        }.create("Home")
        assertEquals(SpaceDraft("Home"), requests.last().decodedBody<SpaceDraft>())
        assertEquals(Space("s1", "Home", Role.OWNER, isPersonal = false, memberCount = 1), created.success())

        repository { noContent() }.setRole("s1", "u2", Role.VIEWER)
        assertEquals(RoleChange(Role.VIEWER), requests.last().decodedBody<RoleChange>())

        val link = repository { json("""{"token":"tok","expiresAt":5}""") }.invite("s1", Role.EDITOR)
        assertEquals(InviteRequest(Role.EDITOR), requests.last().decodedBody<InviteRequest>())
        assertEquals(InviteLink("tok", 5), link.success())

        val members = repository {
            json("""[{"uid":"u2","displayName":null,"email":"b@example.com","role":"viewer","joinedAt":3}]""")
        }.members("s1")
        assertEquals(listOf(Member("u2", null, "b@example.com", Role.VIEWER, 3)), members.success())

        val preview = repository {
            json("""{"spaceName":"Home","inviterName":"Ada","role":"editor","state":"used"}""")
        }.preview("tok")
        assertEquals(InvitePreview("Home", "Ada", Role.EDITOR, InviteState.USED), preview.success())
    }

    // A used invite answers 409, an expired one 410: both reach the screen as HTTP errors with their code
    @Test
    fun usedAndExpiredInvitesAreHttpErrors() = apiTest {
        val error = """{"error":"x","code":"conflict"}"""

        val used = repository { json(error, HttpStatusCode.Conflict) }.accept("tok")
        assertEquals(DataError.Http(409), assertIs<RestResult.Error>(used).error)

        val expired = repository { json(error, HttpStatusCode.Gone) }.accept("tok")
        assertEquals(DataError.Http(410), assertIs<RestResult.Error>(expired).error)

        val unknown = repository { json(error, HttpStatusCode.NotFound) }.preview("tok")
        assertEquals(DataError.Http(404), assertIs<RestResult.Error>(unknown).error)
    }

    private fun <T : Any> RestResult<T>.success(): T = assertIs<RestResult.Success<T>>(this).result

    private companion object {
        const val BASE = "https://example.com/api/"
    }
}
