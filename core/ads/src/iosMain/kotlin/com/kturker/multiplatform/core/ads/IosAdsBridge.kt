package com.kturker.multiplatform.core.ads

import platform.UIKit.UIView

// Implemented in Swift (iosApp/iosApp/AdsBridge.swift) on top of the Google Mobile Ads SDK
// (Swift Package Manager) and registered at launch: Kotlin/Native cannot link the SDK directly.
interface IosAdsBridge {
    // Consent, like a full-screen ad, loads first and is presented later, once Kotlin sees its screen resumed
    // (see presentWhenResumed). Updates the consent information and loads the UMP consent form if the user must
    // answer it. onReady: a form or the App Tracking Transparency prompt waits for presentConsent. onComplete:
    // nothing to ask. Every consent callback reports canRequestAds and privacyOptionsRequired.
    fun loadConsent(
        onReady: () -> Unit,
        onComplete: (Boolean, Boolean) -> Unit
    )

    // Presents the consent form loaded last (if any), then the App Tracking Transparency prompt.
    fun presentConsent(onComplete: (Boolean, Boolean) -> Unit)

    fun showPrivacyOptions(onComplete: (Boolean, Boolean) -> Unit)

    fun makeBanner(adUnitId: String): UIView

    // A full-screen ad loads first and opens later, once Kotlin sees its screen resumed (see presentWhenResumed).
    fun loadInterstitial(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    )

    // Opens the interstitial loaded last.
    fun presentInterstitial(
        onDismissed: () -> Unit,
        onFailed: (String) -> Unit
    )

    fun loadRewarded(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    )

    // Opens the rewarded ad loaded last.
    fun presentRewarded(
        onReward: (Int, String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (String) -> Unit
    )

    // The caller stopped waiting: a consent form or an ad still loading, or loaded but not presented yet, is
    // dropped, and a privacy options form still loading for it is not presented. A form or ad already on screen
    // stays until the user closes it; only its result is dropped. The next request starts afresh.
    fun cancelPending()
}

object IosAds {
    private var bridge: IosAdsBridge? = null

    fun register(bridge: IosAdsBridge) {
        this.bridge = bridge
    }

    internal fun requireBridge(): IosAdsBridge = checkNotNull(bridge) {
        "IosAdsBridge is not registered: call registerAdsBridge(bridge:) in iOSApp.init()"
    }
}
