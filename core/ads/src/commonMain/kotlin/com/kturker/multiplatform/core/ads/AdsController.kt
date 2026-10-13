package com.kturker.multiplatform.core.ads

import androidx.compose.runtime.Composable

// Consent first (UMP, then ATT on iOS), then the Mobile Ads SDK starts. The demo asks on the
// first visit to the ads screen; a real app usually gathers consent at launch.
interface AdsController {
    // The consent form (and ATT on iOS) opens only while the screen that created the controller
    // (rememberAdsController) is resumed. A form that has not opened yet when the call is cancelled or that
    // screen is destroyed is dropped likewise: it never opens, and the call ends with a cancellation.
    suspend fun gatherConsent(): ConsentStatus

    // The UMP privacy options form, where the user changes the consent given earlier. Returns the
    // consent once the form is closed.
    suspend fun showPrivacyOptions(): ConsentStatus

    // A loaded ad opens only while the screen that created the controller is resumed. If the call is
    // cancelled or that screen is destroyed before the ad opens, the call ends with a cancellation
    // (CancellationException) and returns no AdResult. An ad already on screen is never cancelled and its
    // result still arrives.
    suspend fun showInterstitial(): AdResult

    // Gated like showInterstitial.
    suspend fun showRewarded(): AdResult
}

// privacyOptionsRequired: the app must offer an entry to the privacy options form (GDPR regions).
data class ConsentStatus(val canRequestAds: Boolean, val privacyOptionsRequired: Boolean)

@Composable
expect fun rememberAdsController(): AdsController
