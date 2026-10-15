package com.kturker.multiplatform.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import java.io.File

/** Runs [block] and returns the WARN-or-higher log lines it produced, formatted. */
fun capturedWarnings(block: () -> Unit): List<String> {
    val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
    val appender = ListAppender<ILoggingEvent>().apply { start() }
    root.addAppender(appender)
    try {
        block()
    } finally {
        root.detachAppender(appender)
    }
    return appender.list.filter { it.level.isGreaterOrEqual(Level.WARN) }.map { it.formattedMessage }
}

/** Runs [block] with [dir] read-only, so nothing in it can be removed; the permission is restored afterwards. */
inline fun <T> withReadOnlyDir(dir: File, block: () -> T): T {
    check(dir.setWritable(false)) { "could not make $dir read-only" }
    try {
        check(!dir.canWrite()) { "$dir is still writable (running as root?)" }
        return block()
    } finally {
        dir.setWritable(true)
    }
}
