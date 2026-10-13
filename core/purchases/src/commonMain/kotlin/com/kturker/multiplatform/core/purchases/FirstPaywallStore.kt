package com.kturker.multiplatform.core.purchases

// The flag FirstLaunchPaywall writes once it has opened the paywall. Clear the app's data to see it again.
const val PAYWALL_FIRST_SHOWN_KEY = "paywall_first_shown"

// A small flag on the device (SharedPreferences, NSUserDefaults): it works without core/database, which not
// every project has
interface FirstPaywallStore {
    fun isShown(): Boolean

    fun markShown()
}

expect fun getPlatformFirstPaywallStore(): FirstPaywallStore
