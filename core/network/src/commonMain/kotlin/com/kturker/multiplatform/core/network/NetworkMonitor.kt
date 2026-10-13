package com.kturker.multiplatform.core.network

import com.kturker.networkmonitor.MonitorLogging
import io.ktor.client.HttpClientConfig

/**
 * Whether the network monitor may record calls. Android: always, because release APKs link
 * `networkmonitor-noop` (see androidApp/build.gradle.kts). iOS: debug binaries only.
 */
internal expect val isNetworkMonitorEnabled: Boolean

internal fun HttpClientConfig<*>.installNetworkMonitor() {
    if (!isNetworkMonitorEnabled) return
    install(MonitorLogging) {
        sanitizeHeader { header -> SENSITIVE_HEADERS.any { it.equals(header, ignoreCase = true) } }
    }
}
