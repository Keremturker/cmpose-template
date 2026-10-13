package com.kturker.multiplatform.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Request lines carry URLs: they are written in debug builds only
class RequestLoggingTest {

    @Test
    fun aDebugBuildLogsRequests() {
        HttpClient(getPlatformEngine()) { installRequestLogging(isDebug = true) }.use { client ->
            assertNotNull(client.pluginOrNull(Logging))
        }
    }

    @Test
    fun aReleaseBuildDoesNotLogRequests() {
        HttpClient(getPlatformEngine()) { installRequestLogging(isDebug = false) }.use { client ->
            assertNull(client.pluginOrNull(Logging))
        }
    }
}
