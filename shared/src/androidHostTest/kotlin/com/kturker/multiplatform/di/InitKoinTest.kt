package com.kturker.multiplatform.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.kturker.multiplatform.core.domain.BuildEnvironment
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The build type the platform entry point passes to initKoin reaches the graph. */
class InitKoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun theEntryPointsBuildTypeReachesTheGraph() {
        initKoin(isDebug = false)

        assertEquals(BuildEnvironment(isDebug = false), KoinPlatform.getKoin().get<BuildEnvironment>())
    }

    @Test
    fun aDebugEntryPointReachesTheGraphToo() {
        initKoin(isDebug = true)

        assertEquals(BuildEnvironment(isDebug = true), KoinPlatform.getKoin().get<BuildEnvironment>())
        assertEquals(Severity.Verbose, Logger.config.minSeverity)
    }

    @Test
    fun aReleaseEntryPointLogsOnlyWarningsAndErrors() {
        initKoin(isDebug = false)

        assertEquals(Severity.Warn, Logger.config.minSeverity)
    }
}
