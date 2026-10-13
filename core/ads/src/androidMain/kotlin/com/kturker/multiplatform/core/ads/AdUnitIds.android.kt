package com.kturker.multiplatform.core.ads

// Google's test AdMob app ID. The GMA Next-Gen SDK takes the app ID at start (AdsController.android.kt);
// UMP still reads it from AndroidManifest.xml, so replace both with the same ID before you ship.
internal const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

actual object AdUnitIds {
    actual val banner: String = "ca-app-pub-3940256099942544/6300978111"
    actual val interstitial: String = "ca-app-pub-3940256099942544/1033173712"
    actual val rewarded: String = "ca-app-pub-3940256099942544/5224354917"
}
