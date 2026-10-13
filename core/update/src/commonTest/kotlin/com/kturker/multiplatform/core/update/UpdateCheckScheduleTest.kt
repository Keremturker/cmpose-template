package com.kturker.multiplatform.core.update

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val NOW = 1_800_000_000_000L
private const val ONE_HOUR = 60L * 60 * 1000

class UpdateCheckScheduleTest {

    @Test
    fun theFirstLaunchChecks() {
        assertTrue(isCheckDue(lastCheckEpochMillis = null, nowEpochMillis = NOW, lastWasRequired = false))
    }

    @Test
    fun lessThanADayLaterDoesNotCheck() {
        assertFalse(
            isCheckDue(lastCheckEpochMillis = NOW - 23 * ONE_HOUR, nowEpochMillis = NOW, lastWasRequired = false)
        )
    }

    @Test
    fun aDayLaterChecks() {
        assertTrue(
            isCheckDue(lastCheckEpochMillis = NOW - 24 * ONE_HOUR, nowEpochMillis = NOW, lastWasRequired = false)
        )
    }

    // Reopening the app must not get past a required update
    @Test
    fun aRequiredUpdateChecksOnEveryLaunch() {
        assertTrue(isCheckDue(lastCheckEpochMillis = NOW - ONE_HOUR, nowEpochMillis = NOW, lastWasRequired = true))
    }
}
