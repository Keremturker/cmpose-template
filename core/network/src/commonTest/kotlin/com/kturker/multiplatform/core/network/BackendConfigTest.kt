package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.BuildEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackendConfigTest {

    // Request paths have no leading slash, so a base URL without a trailing one would lose its last segment
    @Test
    fun baseUrlsEndWithSlash() {
        assertTrue(BackendConfig.baseUrl(BuildEnvironment(isDebug = true)).endsWith("/"))
        assertTrue(BackendConfig.baseUrl(BuildEnvironment(isDebug = false)).endsWith("/"))
    }

    @Test
    fun debugBuildsUseTheLocalServer() {
        assertEquals(
            "http://${localServerHost()}:18001/",
            BackendConfig.baseUrl(BuildEnvironment(isDebug = true))
        )
    }

    // No live server yet: release builds get a placeholder that can never resolve, over HTTPS only
    @Test
    fun releaseBuildsUseTheReleaseBaseUrl() {
        assertEquals("https://example.invalid/", BackendConfig.baseUrl(BuildEnvironment(isDebug = false)))
        assertTrue(BackendConfig.RELEASE_BASE_URL.startsWith("https://"))
    }

    @Test
    fun theLocalServerHostIsOneTheDebugBuildMayReachOverPlainHttp() {
        assertTrue(localServerHost() in setOf("10.0.2.2", "localhost"))
    }
}
