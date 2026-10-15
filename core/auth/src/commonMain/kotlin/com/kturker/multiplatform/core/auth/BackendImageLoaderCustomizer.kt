package com.kturker.multiplatform.core.auth

import coil3.ComponentRegistry
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.kturker.multiplatform.core.image.ImageLoaderCustomizer
import io.ktor.client.HttpClient

/**
 * Network images load through the backend's HttpClient, which sends its bearer token to the backend's host only,
 * so the backend's own images (GET files/{id}) load. It comes before the fetcher coil-network-ktor3 registers on
 * its own.
 */
internal class BackendImageLoaderCustomizer(private val backendClient: HttpClient) : ImageLoaderCustomizer {
    override fun ComponentRegistry.Builder.customize() {
        add(KtorNetworkFetcherFactory(httpClient = { backendClient }))
    }
}
