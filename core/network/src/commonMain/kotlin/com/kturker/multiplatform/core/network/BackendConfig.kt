package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.BuildEnvironment

/**
 * The app's own backend (the `server` module), apart from [ApiConfig]'s public API. Debug builds use the
 * local server (`./gradlew :server:runLocal`), release builds `RELEASE_BASE_URL`. Keep the trailing slash and
 * write request paths without a leading one (core:api ApiRoutes).
 */
object BackendConfig {
    const val LOCAL_SERVER_PORT = 18_001

    /** A placeholder that never resolves: set it to the live server's HTTPS address before you ship. */
    const val RELEASE_BASE_URL = "https://example.invalid/"

    fun baseUrl(environment: BuildEnvironment): String =
        selectBaseUrl(environment.isDebug, "http://${localServerHost()}:$LOCAL_SERVER_PORT/", RELEASE_BASE_URL)
}
