package com.kturker.multiplatform.feature.home.data.service

import com.kturker.multiplatform.feature.home.data.endpoint.GithubEndpoints
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.HttpMethod

internal fun HttpRequestBuilder.reqUserDetail(username: String) {
    url(urlString = "${GithubEndpoints.GetUserDetail.URL}/$username")

    method = HttpMethod.Get
}
