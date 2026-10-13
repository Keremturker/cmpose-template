package com.kturker.multiplatform.core.logging

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import co.touchlab.kermit.platformLogWriter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKermitApi::class)
class LoggingConfigTest {

    private val writer = TestLogWriter(loggable = Severity.Verbose)

    @AfterTest
    fun tearDown() {
        configureLogging(isDebug = true, logWriter = platformLogWriter())
    }

    @Test
    fun aDebugBuildWritesEveryLevel() {
        configureLogging(isDebug = true, logWriter = writer)

        Logger.withTag("Test").v { "verbose" }

        assertEquals(listOf(Severity.Verbose), writer.logs.map { it.severity })
    }

    @Test
    fun aReleaseBuildDropsDebugAndInfo() {
        configureLogging(isDebug = false, logWriter = writer)
        val log = Logger.withTag("Test")

        log.d { "debug" }
        log.i { "info" }

        writer.assertCount(0)
    }

    @Test
    fun aReleaseBuildKeepsWarningsAndErrors() {
        configureLogging(isDebug = false, logWriter = writer)
        val log = Logger.withTag("Test")

        log.w { "warning" }
        log.e { "error" }

        assertEquals(listOf(Severity.Warn, Severity.Error), writer.logs.map { it.severity })
    }

    // The generator writes the project's name into LOG_TAG_PREFIX, so the test reads the constant
    @Test
    fun tagsStartWithTheProjectName() {
        configureLogging(isDebug = true, logWriter = writer)

        Logger.withTag("Network").i { "request" }

        assertEquals("$LOG_TAG_PREFIX/Network", writer.logs.single().tag)
    }

    // Classes create their logger when they load, which can be before initKoin configures Kermit
    @Test
    fun aLoggerCreatedBeforeConfigurationFollowsIt() {
        val early = Logger.withTag("Early")
        configureLogging(isDebug = false, logWriter = writer)

        early.d { "dropped" }
        early.w { "kept" }

        assertEquals(listOf("kept"), writer.logs.map { it.message })
    }
}
