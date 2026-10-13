package com.kturker.multiplatform.core.domain

/**
 * The build the app is running. The platform entry points pass it to `initKoin`: Android reads
 * `FLAG_DEBUGGABLE`, iOS `Platform.isDebugBinary`.
 */
data class BuildEnvironment(val isDebug: Boolean)
