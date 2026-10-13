package com.kturker.multiplatform.core.network

import com.kturker.multiplatform.core.domain.BuildEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiConfigTest {

    @Test
    fun debugBuildsUseTheDebugBaseUrl() {
        assertEquals(ApiConfig.DEBUG_BASE_URL, ApiConfig.baseUrl(BuildEnvironment(isDebug = true)))
    }

    @Test
    fun releaseBuildsUseTheReleaseBaseUrl() {
        assertEquals(ApiConfig.RELEASE_BASE_URL, ApiConfig.baseUrl(BuildEnvironment(isDebug = false)))
    }

    @Test
    fun selectBaseUrlPicksTheDebugUrlForDebugBuilds() {
        assertEquals("https://d.example", selectBaseUrl(true, "https://d.example", "https://r.example"))
    }

    @Test
    fun selectBaseUrlPicksTheReleaseUrlForReleaseBuilds() {
        assertEquals("https://r.example", selectBaseUrl(false, "https://d.example", "https://r.example"))
    }
}
