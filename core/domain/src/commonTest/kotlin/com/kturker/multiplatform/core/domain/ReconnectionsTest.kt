package com.kturker.multiplatform.core.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ReconnectionsTest {

    @Test
    fun comingBackOnlineIsAReconnection() = runTest {
        val online = MutableStateFlow(false)

        online.reconnections().test {
            online.value = true
            awaitItem()
            expectNoEvents()
        }
    }

    // Starting online is not a reconnection: nothing failed for lack of a connection yet
    @Test
    fun startingOnlineIsNot() = runTest {
        MutableStateFlow(true).reconnections().test { expectNoEvents() }
    }

    @Test
    fun goingOfflineIsNot() = runTest {
        val online = MutableStateFlow(true)

        online.reconnections().test {
            online.value = false
            expectNoEvents()
        }
    }
}
