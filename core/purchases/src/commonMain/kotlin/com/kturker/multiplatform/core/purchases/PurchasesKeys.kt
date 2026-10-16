package com.kturker.multiplatform.core.purchases

// The RevenueCat API key of this platform: revenueCatApiKey in PurchasesKeys.android.kt and PurchasesKeys.ios.kt.
// Both ship the Test Store key of the cmpose demo project: purchases are simulated, nothing is charged.
// A release build with a Test Store key keeps purchases disabled on both platforms: RevenueCat's Android
// SDK closes a release app configured with one. purchases-kmp builds purchases-ios with that check
// bypassed, but the template disables purchases on iOS too, for the same behavior on both platforms and
// in case a future SDK enforces it. Before you ship, put your own keys in: goog_... in androidMain,
// appl_... in iosMain. See README "Monetization".
internal expect val revenueCatApiKey: String

object PurchasesKeys {
    private const val TEST_STORE_PREFIX = "test_"

    fun isTestStoreKey(apiKey: String): Boolean = apiKey.startsWith(TEST_STORE_PREFIX)

    // Debug builds take any key; release builds only a platform key.
    fun canConfigure(
        apiKey: String,
        isDebug: Boolean
    ): Boolean = isDebug || !isTestStoreKey(apiKey)
}
