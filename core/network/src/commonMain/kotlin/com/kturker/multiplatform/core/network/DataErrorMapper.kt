package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.DataError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * The reason behind a failed request. With expectSuccess a status outside 2xx is a ResponseException;
 * Ktor's timeouts (HttpRequestTimeoutException, ConnectTimeoutException, SocketTimeoutException) and
 * the engines' connection errors are IOExceptions. Callers rethrow CancellationException first.
 */
fun Throwable.toDataError(): DataError = when (this) {
    is ResponseException -> DataError.Http(code = response.status.value)
    is IOException -> DataError.Network
    is NoTransformationFoundException, is ContentConvertException -> DataError.Serialization
    is SerializationException -> DataError.Serialization
    else -> DataError.Unknown(cause = this)
}
