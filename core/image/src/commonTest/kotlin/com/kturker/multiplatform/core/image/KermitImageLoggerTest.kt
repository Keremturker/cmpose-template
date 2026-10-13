package com.kturker.multiplatform.core.image

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import co.touchlab.kermit.platformLogWriter
import coil3.util.Logger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalKermitApi::class)
class KermitImageLoggerTest {

    private val writer = TestLogWriter(loggable = Severity.Verbose)

    @BeforeTest
    fun setUp() {
        co.touchlab.kermit.Logger.setLogWriters(writer)
        co.touchlab.kermit.Logger.setMinSeverity(Severity.Verbose)
    }

    @AfterTest
    fun tearDown() {
        co.touchlab.kermit.Logger.setLogWriters(platformLogWriter())
    }

    @Test
    fun everyCoilLevelHasAKermitSeverity() {
        assertEquals(Severity.Verbose, Logger.Level.Verbose.toSeverity())
        assertEquals(Severity.Debug, Logger.Level.Debug.toSeverity())
        assertEquals(Severity.Info, Logger.Level.Info.toSeverity())
        assertEquals(Severity.Warn, Logger.Level.Warn.toSeverity())
        assertEquals(Severity.Error, Logger.Level.Error.toSeverity())
    }

    @Test
    fun releaseBuildsGetNoImageLogger() {
        assertNull(imageLogger(isDebug = false))
        assertNotNull(imageLogger(isDebug = true))
    }

    // The exception can carry the image URL and a stack trace; only Coil's message is written
    @Test
    fun aCoilLineIsWrittenUnderTheImageTagWithoutItsException() {
        KermitImageLogger().log(
            tag = "RealImageLoader",
            level = Logger.Level.Error,
            message = "Failed",
            throwable = IllegalStateException("x")
        )

        val line = writer.logs.single()
        assertEquals("Image", line.tag)
        assertEquals(Severity.Error, line.severity)
        assertEquals("RealImageLoader: Failed", line.message)
        assertNull(line.throwable)
    }
}
