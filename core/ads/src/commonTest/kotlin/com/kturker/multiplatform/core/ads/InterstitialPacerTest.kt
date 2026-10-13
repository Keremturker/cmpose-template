package com.kturker.multiplatform.core.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class InterstitialPacerTest {

    private val time = TestTimeSource()

    private fun pacer(maxPerSession: Int = InterstitialPacer.DEFAULT_MAX_PER_SESSION) =
        InterstitialPacer(timeSource = time, minSessionAge = 60.seconds, maxPerSession = maxPerSession)

    @Test
    fun notInTheFirstMinute() {
        val pacer = pacer()
        time += 59.seconds

        assertFalse(pacer.shouldShow())
    }

    @Test
    fun afterTheFirstMinute() {
        val pacer = pacer()
        time += 60.seconds

        assertTrue(pacer.shouldShow())
    }

    @Test
    fun atMostOnePerSessionByDefault() {
        val pacer = pacer()
        time += 61.seconds
        pacer.recordShown()
        time += 1.hours

        assertFalse(pacer.shouldShow())
    }

    @Test
    fun countsUpToTheSessionLimit() {
        val pacer = pacer(maxPerSession = 2)
        time += 2.minutes

        pacer.recordShown()
        assertTrue(pacer.shouldShow())
        pacer.recordShown()
        assertFalse(pacer.shouldShow())
    }

    @Test
    fun theSessionStartsWithThePacer() {
        time += 10.minutes
        val pacer = pacer()

        assertFalse(pacer.shouldShow())
    }

    @Test
    fun defaultsAreAMinuteAndOne() {
        assertEquals(60.seconds, InterstitialPacer.DEFAULT_MIN_SESSION_AGE)
        assertEquals(1, InterstitialPacer.DEFAULT_MAX_PER_SESSION)
    }
}
