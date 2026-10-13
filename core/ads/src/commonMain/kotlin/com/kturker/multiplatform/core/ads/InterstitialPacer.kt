package com.kturker.multiplatform.core.ads

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Keeps interstitials to natural breaks: none in the first [minSessionAge] of the session (the app's process,
 * counted from this pacer's creation) and at most [maxPerSession] per session. At a natural break (a level
 * finished, a list saved) ask [shouldShow]; after the ad was on screen call [recordShown]. Premium (ad-free)
 * is the caller's check, not the pacer's. Koin gives the app one pacer (provideInterstitialPacer).
 */
class InterstitialPacer(
    timeSource: TimeSource = TimeSource.Monotonic,
    private val minSessionAge: Duration = DEFAULT_MIN_SESSION_AGE,
    private val maxPerSession: Int = DEFAULT_MAX_PER_SESSION
) {
    private val sessionStart = timeSource.markNow()
    private var shownThisSession = 0

    fun shouldShow(): Boolean =
        shownThisSession < maxPerSession && sessionStart.elapsedNow() >= minSessionAge

    fun recordShown() {
        shownThisSession++
    }

    companion object {
        val DEFAULT_MIN_SESSION_AGE: Duration = 60.seconds
        const val DEFAULT_MAX_PER_SESSION: Int = 1
    }
}
