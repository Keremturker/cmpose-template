package com.kturker.multiplatform.core.ads

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.UIKit.UIView
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The Swift bridge reports the reward and the dismissal separately and in no documented order. An ad
// opens only while its screen is resumed.
@OptIn(ExperimentalCoroutinesApi::class)
class IosAdsControllerTest {

    private val bridge = FakeIosAdsBridge().also { IosAds.register(it) }
    private val screen = TestScreen()
    private val reward = AdResult.Rewarded(amount = 10, type = "coins")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        screen.moveTo(Lifecycle.State.RESUMED)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.requestRewarded() =
        async { IosAdsController(screen.lifecycle).showRewarded() }.also { runCurrent() }

    // Requested and loaded on the resumed screen: the ad is on screen
    private fun TestScope.showRewarded() =
        requestRewarded().also {
            bridge.load()
            runCurrent()
            assertEquals(1, bridge.presented)
        }

    @Test
    fun aRewardBeforeTheDismissalCounts() = runTest {
        val result = showRewarded()

        bridge.reward(reward)
        bridge.dismiss()
        runCurrent()

        assertTrue(result.isCompleted)
        assertEquals(reward, result.await())
    }

    @Test
    fun aRewardAfterTheDismissalWithinTheGraceCounts() = runTest {
        val result = showRewarded()

        bridge.dismiss()
        advanceTimeBy(LATE_REWARD_GRACE.inWholeMilliseconds - 100)
        assertFalse(result.isCompleted)
        bridge.reward(reward)
        runCurrent()

        assertEquals(reward, result.await())
    }

    @Test
    fun noRewardWithinTheGraceIsADismissal() = runTest {
        val result = showRewarded()

        bridge.dismiss()
        advanceTimeBy(LATE_REWARD_GRACE.inWholeMilliseconds + 1)
        bridge.reward(reward)

        assertEquals(AdResult.Dismissed, result.await())
    }

    @Test
    fun aFailureIsNotHeldByTheGrace() = runTest {
        val result = showRewarded()

        bridge.fail("no fill")
        runCurrent()

        assertEquals(AdResult.Failed("no fill"), result.await())
    }

    @Test
    fun leavingDuringTheGraceCancelsWithoutAResult() = runTest {
        val result = showRewarded()

        bridge.dismiss()
        runCurrent()
        result.cancel()
        bridge.reward(reward)
        runCurrent()

        assertTrue(result.isCancelled)
    }

    @Test
    fun leavingBeforeTheDismissalCancelsThePendingAd() = runTest {
        val result = showRewarded()

        result.cancel()
        runCurrent()

        assertTrue(result.isCancelled)
        assertEquals(1, bridge.cancelled)
    }

    @Test
    fun aLoadFailureIsTheResult() = runTest {
        val result = requestRewarded()

        bridge.failLoad("no fill")
        runCurrent()

        assertEquals(AdResult.Failed("no fill"), result.await())
        assertEquals(0, bridge.presented)
    }

    // Back starts the exit transition: the screen leaves RESUMED at once but stays composed for a while.
    @Test
    fun anAdLoadedAfterBackIsNotPresented() = runTest {
        val result = requestRewarded()

        screen.moveTo(Lifecycle.State.CREATED)
        bridge.load()
        runCurrent()
        assertEquals(0, bridge.presented)
        // The transition ends and the screen's call is cancelled
        result.cancel()
        runCurrent()

        assertEquals(0, bridge.presented)
        assertEquals(1, bridge.cancelled)
    }

    // The bridge reported "loaded" but the caller is cancelled before the coroutine resumed
    @Test
    fun aCallCancelledRightAfterTheLoadDropsTheLoadedAd() = runTest {
        val result = requestRewarded()

        bridge.load()
        result.cancel()
        runCurrent()

        assertTrue(result.isCancelled)
        assertEquals(0, bridge.presented)
        assertEquals(1, bridge.cancelled)
    }

    @Test
    fun anInterstitialIsLoadedPresentedAndDismissed() = runTest {
        val result = async { IosAdsController(screen.lifecycle).showInterstitial() }
        runCurrent()

        bridge.load()
        runCurrent()
        assertEquals(1, bridge.presented)
        bridge.dismiss()
        runCurrent()

        assertEquals(AdResult.Dismissed, result.await())
        assertEquals(0, bridge.cancelled)
    }

    @Test
    fun anAdLoadedWhileTheAppIsAwayIsPresentedOnReturn() = runTest {
        val result = requestRewarded()

        screen.moveTo(Lifecycle.State.CREATED)
        bridge.load()
        runCurrent()
        screen.moveTo(Lifecycle.State.RESUMED)
        runCurrent()
        assertEquals(1, bridge.presented)
        bridge.reward(reward)
        bridge.dismiss()
        runCurrent()

        assertEquals(reward, result.await())
    }

    // A presented view controller stops the Compose screen under it: the ad keeps its result.
    @Test
    fun anAdOnScreenKeepsItsRewardWhileItStopsTheScreen() = runTest {
        val result = showRewarded()

        screen.moveTo(Lifecycle.State.CREATED)
        runCurrent()
        bridge.reward(reward)
        bridge.dismiss()
        screen.moveTo(Lifecycle.State.RESUMED)
        runCurrent()

        assertEquals(reward, result.await())
        assertEquals(0, bridge.cancelled)
    }

    private fun TestScope.requestConsent() =
        async { IosAdsController(screen.lifecycle).gatherConsent() }.also { runCurrent() }

    @Test
    fun aConsentFormLoadedOnTheResumedScreenIsPresentedAndReported() = runTest {
        val result = requestConsent()

        bridge.consentReady()
        runCurrent()
        assertEquals(1, bridge.consentPresented)
        bridge.completeConsent(canRequestAds = true, privacyOptionsRequired = true)
        runCurrent()

        assertEquals(ConsentStatus(canRequestAds = true, privacyOptionsRequired = true), result.await())
        assertEquals(0, bridge.cancelled)
    }

    // Back starts the exit transition: neither the form nor the ATT prompt opens over the next screen
    @Test
    fun aConsentFormLoadedAfterBackIsNotPresented() = runTest {
        val result = requestConsent()

        screen.moveTo(Lifecycle.State.CREATED)
        bridge.consentReady()
        runCurrent()
        assertEquals(0, bridge.consentPresented)
        result.cancel()
        runCurrent()

        assertTrue(result.isCancelled)
        assertEquals(0, bridge.consentPresented)
        assertEquals(1, bridge.cancelled)
    }

    // The form is on screen when the caller is cancelled: no status is reported, and the form's late
    // callback is ignored.
    @Test
    fun leavingWhileTheConsentFormIsOnScreenCancelsOnceAndIgnoresTheLateResult() = runTest {
        val result = requestConsent()

        bridge.consentReady()
        runCurrent()
        assertEquals(1, bridge.consentPresented)
        result.cancel()
        runCurrent()
        bridge.completeConsent(canRequestAds = true, privacyOptionsRequired = false)
        runCurrent()

        // Cancelled without a status: awaiting it throws instead of returning a ConsentStatus
        assertTrue(result.isCancelled)
        assertFailsWith<CancellationException> { result.await() }
        assertEquals(1, bridge.cancelled)
    }

    @Test
    fun aConsentFormLoadedWhileTheAppIsAwayIsPresentedOnReturn() = runTest {
        val result = requestConsent()

        screen.moveTo(Lifecycle.State.CREATED)
        bridge.consentReady()
        runCurrent()
        assertEquals(0, bridge.consentPresented)
        screen.moveTo(Lifecycle.State.RESUMED)
        runCurrent()
        assertEquals(1, bridge.consentPresented)
        bridge.completeConsent(canRequestAds = false, privacyOptionsRequired = true)

        assertEquals(ConsentStatus(canRequestAds = false, privacyOptionsRequired = true), result.await())
    }

    @Test
    fun leavingWhileTheConsentLoadsCancelsItOnce() = runTest {
        val result = requestConsent()

        result.cancel()
        runCurrent()
        bridge.consentReady()
        runCurrent()

        assertTrue(result.isCancelled)
        assertEquals(0, bridge.consentPresented)
        assertEquals(1, bridge.cancelled)
    }

    // Outside the EEA in release (or consent given earlier) there is nothing to ask
    @Test
    fun noConsentToAskIsReportedWithoutPresenting() = runTest {
        val result = requestConsent()

        bridge.consentNotNeeded(canRequestAds = true, privacyOptionsRequired = false)
        runCurrent()

        assertEquals(ConsentStatus(canRequestAds = true, privacyOptionsRequired = false), result.await())
        assertEquals(0, bridge.consentPresented)
        assertEquals(0, bridge.cancelled)
    }
}

private class FakeIosAdsBridge : IosAdsBridge {
    private var onLoaded: (() -> Unit)? = null
    private var onLoadFailed: ((String) -> Unit)? = null
    private var onReward: ((Int, String) -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null
    private var onFailed: ((String) -> Unit)? = null
    private var onConsentReady: (() -> Unit)? = null
    private var onConsentComplete: ((Boolean, Boolean) -> Unit)? = null
    var presented = 0
    var consentPresented = 0
    var cancelled = 0

    fun consentReady() = onConsentReady?.invoke()

    fun consentNotNeeded(canRequestAds: Boolean, privacyOptionsRequired: Boolean) =
        onConsentComplete?.invoke(canRequestAds, privacyOptionsRequired)

    fun completeConsent(canRequestAds: Boolean, privacyOptionsRequired: Boolean) =
        onConsentComplete?.invoke(canRequestAds, privacyOptionsRequired)

    fun load() = onLoaded?.invoke()

    fun failLoad(reason: String) = onLoadFailed?.invoke(reason)

    fun reward(reward: AdResult.Rewarded) = onReward?.invoke(reward.amount, reward.type)

    fun dismiss() = onDismissed?.invoke()

    fun fail(reason: String) = onFailed?.invoke(reason)

    override fun loadConsent(
        onReady: () -> Unit,
        onComplete: (Boolean, Boolean) -> Unit
    ) {
        onConsentReady = onReady
        onConsentComplete = onComplete
    }

    override fun presentConsent(onComplete: (Boolean, Boolean) -> Unit) {
        consentPresented += 1
        onConsentComplete = onComplete
    }

    override fun showPrivacyOptions(onComplete: (Boolean, Boolean) -> Unit) = Unit

    override fun makeBanner(adUnitId: String): UIView = error("Not used by these tests")

    override fun loadInterstitial(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        this.onLoaded = onLoaded
        this.onLoadFailed = onFailed
    }

    override fun presentInterstitial(
        onDismissed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        presented += 1
        this.onDismissed = onDismissed
        this.onFailed = onFailed
    }

    override fun loadRewarded(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        this.onLoaded = onLoaded
        this.onLoadFailed = onFailed
    }

    override fun presentRewarded(
        onReward: (Int, String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        presented += 1
        this.onReward = onReward
        this.onDismissed = onDismissed
        this.onFailed = onFailed
    }

    override fun cancelPending() {
        cancelled += 1
    }
}
