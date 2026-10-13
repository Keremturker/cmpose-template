package com.kturker.multiplatform.core.domain

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlowExtensionTest {

    private val notFound = RestResult.Error(DataError.Http(code = 404))

    @Test
    fun onErrorHandsTheReasonToItsActionAndPassesEveryResultOn() = runTest {
        val reasons = mutableListOf<DataError>()

        val results = flowOf<RestResult<String>>(RestResult.Loading(true), notFound, RestResult.Loading(false))
            .onError { error -> reasons += error }
            .toList()

        assertEquals(listOf<DataError>(DataError.Http(code = 404)), reasons)
        assertEquals(3, results.size)
        assertEquals(notFound, results[1])
    }

    @Test
    fun onErrorAcceptsAnActionThatIgnoresTheReason() = runTest {
        var handled = false

        flowOf<RestResult<String>>(notFound).onError { handled = true }.toList()

        assertTrue(handled)
    }
}
