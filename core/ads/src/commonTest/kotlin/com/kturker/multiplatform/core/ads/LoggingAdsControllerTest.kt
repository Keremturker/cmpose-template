package com.kturker.multiplatform.core.ads

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalKermitApi::class)
class LoggingAdsControllerTest {

    private val writer = TestLogWriter(loggable = Severity.Verbose)

    @BeforeTest
    fun setUp() {
        Logger.setLogWriters(writer)
        Logger.setMinSeverity(Severity.Verbose)
    }

    @AfterTest
    fun tearDown() {
        Logger.setLogWriters(platformLogWriter())
    }

    @Test
    fun refusedConsentIsAWarning() = runTest {
        val status = LoggingAdsController(FakeAdsController(canRequestAds = false)).gatherConsent()

        assertFalse(status.canRequestAds)
        assertEquals(listOf(Severity.Warn), writer.logs.map { it.severity })
    }

    @Test
    fun aFailedAdIsAWarningWithItsReason() = runTest {
        val failed = AdResult.Failed(reason = "No fill")

        val result = LoggingAdsController(FakeAdsController(result = failed)).showInterstitial()

        assertEquals(AdResult.Failed(reason = "No fill"), result)
        assertEquals("Interstitial ad failed: No fill", writer.logs.single { it.severity == Severity.Warn }.message)
    }

    @Test
    fun aShownAdWritesNoWarning() = runTest {
        val rewarded = AdResult.Rewarded(amount = 10, type = "coins")

        val result = LoggingAdsController(FakeAdsController(result = rewarded)).showRewarded()

        assertEquals(rewarded, result)
        assertTrue(writer.logs.none { it.severity >= Severity.Warn })
    }
}

private class FakeAdsController(
    private val canRequestAds: Boolean = true,
    private val result: AdResult = AdResult.Dismissed
) : AdsController {

    override suspend fun gatherConsent(): ConsentStatus =
        ConsentStatus(canRequestAds = canRequestAds, privacyOptionsRequired = false)

    override suspend fun showPrivacyOptions(): ConsentStatus =
        ConsentStatus(canRequestAds = canRequestAds, privacyOptionsRequired = false)

    override suspend fun showInterstitial(): AdResult = result

    override suspend fun showRewarded(): AdResult = result
}
