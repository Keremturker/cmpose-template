package com.kturker.multiplatform.core.ads

import co.touchlab.kermit.Logger

private val log = Logger.withTag("Ads")

// Writes what each ads call ended with: the consent result, and why an ad did not load or show.
// rememberAdsController wraps both platforms' controllers in it.
internal class LoggingAdsController(private val delegate: AdsController) : AdsController {

    override suspend fun gatherConsent(): ConsentStatus =
        delegate.gatherConsent().also { logConsent(it) }

    override suspend fun showPrivacyOptions(): ConsentStatus =
        delegate.showPrivacyOptions().also { logConsent(it) }

    override suspend fun showInterstitial(): AdResult =
        delegate.showInterstitial().also { logIfFailed(kind = "Interstitial", result = it) }

    override suspend fun showRewarded(): AdResult =
        delegate.showRewarded().also { logIfFailed(kind = "Rewarded", result = it) }
}

private fun logConsent(status: ConsentStatus) {
    if (status.canRequestAds) {
        log.i { "Consent allows ad requests" }
    } else {
        log.w { "Consent does not allow ad requests: no ads are loaded" }
    }
}

private fun logIfFailed(
    kind: String,
    result: AdResult
) {
    if (result is AdResult.Failed) log.w { "$kind ad failed: ${result.reason}" }
}
