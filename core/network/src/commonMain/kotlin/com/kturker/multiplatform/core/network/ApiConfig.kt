package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.BuildEnvironment

/**
 * The API the app talks to. Debug builds use `DEBUG_BASE_URL`, release builds `RELEASE_BASE_URL`.
 * Keep the trailing slash and write request paths without a leading one.
 */
object ApiConfig {
    const val DEBUG_BASE_URL = "https://api.github.com/"
    const val RELEASE_BASE_URL = "https://api.github.com/"

    fun baseUrl(environment: BuildEnvironment): String =
        selectBaseUrl(environment.isDebug, DEBUG_BASE_URL, RELEASE_BASE_URL)
}

internal fun selectBaseUrl(
    isDebug: Boolean,
    debugUrl: String,
    releaseUrl: String
): String = if (isDebug) debugUrl else releaseUrl
