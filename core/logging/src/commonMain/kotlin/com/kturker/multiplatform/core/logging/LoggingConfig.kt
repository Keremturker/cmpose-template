package com.kturker.multiplatform.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

// Every tag starts with the project's name, so one Logcat or Console filter shows all of the app's lines.
// The generator writes the project's name here.
internal const val LOG_TAG_PREFIX = "Multiplatform"

/**
 * Sets up Kermit once, before Koin starts (`initKoin`). Debug builds write every level, release builds
 * only warnings and errors. Android writes to Logcat, iOS to OSLog. A class logs through its own tagged
 * logger: `private val log = Logger.withTag("Checkout")`. Never log tokens, request or response bodies,
 * headers or user data.
 */
fun configureLogging(isDebug: Boolean) {
    configureLogging(isDebug = isDebug, logWriter = platformLogWriter())
}

internal fun configureLogging(
    isDebug: Boolean,
    logWriter: LogWriter
) {
    Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Warn)
    Logger.setLogWriters(TagPrefixLogWriter(prefix = LOG_TAG_PREFIX, delegate = logWriter))
    Logger.setTag(LOG_TAG_PREFIX)
}
