package com.kturker.multiplatform.core.domain

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UseCaseFlowExtensionTest {

    @Test
    fun aFailingUpstreamBecomesAnUnknownErrorBetweenTheLoadingStates() = runTest {
        val results = flow<RestResult<String>> { throw IllegalStateException("boom") }
            .buildDefaultFlow(UnconfinedTestDispatcher(testScheduler))
            .toList()

        assertEquals(3, results.size)
        assertEquals(true, assertIs<RestResult.Loading>(results[0]).loading)
        val unknown = assertIs<DataError.Unknown>(assertIs<RestResult.Error>(results[1]).error)
        assertEquals("boom", unknown.cause.message)
        assertEquals(false, assertIs<RestResult.Loading>(results[2]).loading)
    }

    // A cancelled request (the screen was left, a newer search started) must not show as a failure
    @Test
    fun cancellationPassesThroughInsteadOfBecomingAnError() = runTest {
        val seen = mutableListOf<RestResult<String>>()

        assertFailsWith<CancellationException> {
            flow<RestResult<String>> { throw CancellationException("screen left") }
                .buildDefaultFlow(UnconfinedTestDispatcher(testScheduler))
                .onEach { seen += it }
                .collect()
        }

        assertIs<RestResult.Loading>(seen.single())
    }
}
