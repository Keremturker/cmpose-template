package com.kturker.multiplatform.feature.monetization.presentation.ads

// The full-screen ads (and the privacy options form) the screen asks the platform ads SDK to show; the
// screen hands the SDK call to AdsDemoViewModel.runAd or runPrivacyOptions, which takes the result. The
// first consent flow is not a command: it is state (AdsDemoUiState.consentResolved).
internal sealed interface AdCommand {
    data object ShowInterstitial : AdCommand

    data object ShowRewarded : AdCommand

    data object ShowPrivacyOptions : AdCommand
}
