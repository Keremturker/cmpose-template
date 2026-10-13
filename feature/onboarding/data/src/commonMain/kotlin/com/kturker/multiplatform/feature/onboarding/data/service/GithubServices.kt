package com.kturker.multiplatform.feature.onboarding.data.service

import com.kturker.multiplatform.feature.onboarding.data.endpoint.GithubEndpoints
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpMethod

internal fun HttpRequestBuilder.reqUsers(username: String) {
    url(urlString = GithubEndpoints.SearchUsers.URL)
    parameter(key = GithubEndpoints.SearchUsers.QUERY, value = username)

    method = HttpMethod.Get
}
