package com.kturker.multiplatform.core.purchases

import platform.Foundation.NSUserDefaults

private class UserDefaultsFirstPaywallStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : FirstPaywallStore {

    override fun isShown(): Boolean = defaults.boolForKey(PAYWALL_FIRST_SHOWN_KEY)

    override fun markShown() {
        defaults.setBool(true, forKey = PAYWALL_FIRST_SHOWN_KEY)
    }
}

actual fun getPlatformFirstPaywallStore(): FirstPaywallStore = UserDefaultsFirstPaywallStore()
