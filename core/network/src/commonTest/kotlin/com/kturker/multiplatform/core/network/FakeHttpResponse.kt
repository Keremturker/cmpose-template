package com.kturker.multiplatform.core.network

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// What Ktor throws for a status outside 2xx when the client sets expectSuccess
internal fun responseException(status: HttpStatusCode): ResponseException =
    ResponseException(FakeHttpResponse(status), cachedResponseText = "")

// Only the status is read; ResponseException's message uses toString(), so `call` is never touched
@OptIn(InternalAPI::class)
internal class FakeHttpResponse(override val status: HttpStatusCode) : HttpResponse() {
    override val call: HttpClientCall
        get() = error("FakeHttpResponse has no call")
    override val version: HttpProtocolVersion = HttpProtocolVersion.HTTP_1_1
    override val requestTime: GMTDate = GMTDate()
    override val responseTime: GMTDate = GMTDate()
    override val rawContent: ByteReadChannel = ByteReadChannel.Empty
    override val headers: Headers = Headers.Empty
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext

    override fun toString(): String = "FakeHttpResponse($status)"
}
