package com.kturker.multiplatform.feature.monetization.presentation.ads

internal data class AdsDemoUiState(
    // The consent flow has finished, with either answer. Until then the screen runs it, again after a recreation.
    val consentResolved: Boolean = false,
    // Consent allows ad requests.
    val consentReady: Boolean = false,
    // The consent rules of the user's region require an entry to the UMP privacy options form.
    val privacyOptionsRequired: Boolean = false,
    // Null until the premium state is known: no banner and no interstitial before that.
    val adFree: Boolean? = null,
    // A full-screen ad is loading or on screen: the buttons wait for its result.
    val adBusy: Boolean = false,
    val coins: Int = 0,
    val message: String? = null
) {
    val canShowAds: Boolean get() = consentReady && !adBusy

    // Waits for the premium state: a premium user must not see an interstitial.
    val canShowInterstitial: Boolean get() = canShowAds && adFree != null

    val showPrivacyOptionsButton: Boolean get() = privacyOptionsRequired

    val showBanner: Boolean get() = consentReady && adFree == false
}
