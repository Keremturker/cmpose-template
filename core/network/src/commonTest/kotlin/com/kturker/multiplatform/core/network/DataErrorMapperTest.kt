package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.DataError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorMapperTest {

    @Test
    fun aStatusOutside2xxKeepsItsCode() {
        assertEquals(DataError.Http(code = 404), responseException(HttpStatusCode.NotFound).toDataError())
        assertEquals(DataError.Http(code = 500), responseException(HttpStatusCode.InternalServerError).toDataError())
    }

    // ResponseException is an IllegalStateException: it must not fall through to Unknown
    @Test
    fun githubsRateLimitStatusesKeepTheirCode() {
        assertEquals(DataError.Http(code = 403), responseException(HttpStatusCode.Forbidden).toDataError())
        assertEquals(DataError.Http(code = 429), responseException(HttpStatusCode.TooManyRequests).toDataError())
    }

    @Test
    fun noConnectionAndEveryTimeoutAreNetworkErrors() {
        val failures = listOf(
            IOException("Unable to resolve host api.github.com"),
            HttpRequestTimeoutException("https://api.github.com/users/octocat", 45_000L),
            ConnectTimeoutException("connect timeout"),
            SocketTimeoutException("read timeout")
        )

        failures.forEach { failure -> assertEquals(DataError.Network, failure.toDataError(), failure.toString()) }
    }

    @Test
    fun aBodyThatCannotBeReadIsASerializationError() {
        assertEquals(DataError.Serialization, JsonConvertException("Illegal input").toDataError())
        assertEquals(DataError.Serialization, SerializationException("Field 'login' is required").toDataError())
    }

    @Test
    fun anythingElseIsUnknownAndKeepsItsCause() {
        val boom = IllegalStateException("boom")

        assertEquals(DataError.Unknown(cause = boom), boom.toDataError())
    }
}
