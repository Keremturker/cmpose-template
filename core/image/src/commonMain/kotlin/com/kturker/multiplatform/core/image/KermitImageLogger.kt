package com.kturker.multiplatform.core.image

import co.touchlab.kermit.Severity
import coil3.util.Logger

private val log = co.touchlab.kermit.Logger.withTag("Image")

// Debug builds only: Coil's lines (they can carry an image URL, like Ktor's debug request lines)
internal fun imageLogger(isDebug: Boolean): Logger? = if (isDebug) KermitImageLogger() else null

internal class KermitImageLogger(override var minLevel: Logger.Level = Logger.Level.Debug) : Logger {
    override fun log(
        tag: String,
        level: Logger.Level,
        message: String?,
        throwable: Throwable?
    ) {
        log.log(severity = level.toSeverity(), tag = "Image", throwable = null, message = "$tag: ${message.orEmpty()}")
    }
}

internal fun Logger.Level.toSeverity(): Severity = when (this) {
    Logger.Level.Verbose -> Severity.Verbose
    Logger.Level.Debug -> Severity.Debug
    Logger.Level.Info -> Severity.Info
    Logger.Level.Warn -> Severity.Warn
    Logger.Level.Error -> Severity.Error
}
