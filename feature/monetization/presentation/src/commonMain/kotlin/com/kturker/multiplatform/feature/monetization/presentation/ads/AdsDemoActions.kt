package com.kturker.multiplatform.feature.monetization.presentation.ads

import com.kturker.multiplatform.core.ads.ConsentStatus

internal interface AdsDemoActions {
    fun onConsentResult(status: ConsentStatus)

    fun onPrivacyOptionsClick()

    fun onShowInterstitialClick()

    fun onWatchRewardedClick()

    fun onNavigateUp()
}
