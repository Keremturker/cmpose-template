package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.RestResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

// The HttpClient sets expectSuccess, so a status outside 2xx never gets here: Ktor throws a
// ResponseException, which BaseRepository.request turns into DataError.Http
suspend inline fun <reified T> HttpResponse.asRestResult(): RestResult<T> =
    RestResult.Success(body<T>())
