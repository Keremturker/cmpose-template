package com.kturker.multiplatform.core.ads

// Google's public test ad units: they always fill and never pay. Replace them with your AdMob
// units before you ship (README "Monetization"). The AdMob app ID lives in AndroidManifest.xml plus
// ADMOB_APP_ID (AdUnitIds.android.kt) on Android and in iosApp/iosApp/Info.plist on iOS.
expect object AdUnitIds {
    val banner: String
    val interstitial: String
    val rewarded: String
}
