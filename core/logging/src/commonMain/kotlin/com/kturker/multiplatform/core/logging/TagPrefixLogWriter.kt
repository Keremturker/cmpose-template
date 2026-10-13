package com.kturker.multiplatform.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

// Puts the project's name in front of each tag: Logger.withTag("Network") writes as "<Project>/Network",
// an untagged line as "<Project>"
internal class TagPrefixLogWriter(private val prefix: String, private val delegate: LogWriter) : LogWriter() {

    override fun isLoggable(
        tag: String,
        severity: Severity
    ): Boolean = delegate.isLoggable(tag = prefixed(tag), severity = severity)

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) =
        delegate.log(severity = severity, message = message, tag = prefixed(tag), throwable = throwable)

    private fun prefixed(tag: String): String =
        if (tag.isEmpty() || tag == prefix) prefix else "$prefix/$tag"
}
