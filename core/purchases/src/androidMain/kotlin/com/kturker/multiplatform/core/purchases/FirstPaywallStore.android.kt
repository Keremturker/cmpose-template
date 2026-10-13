package com.kturker.multiplatform.core.purchases

import android.content.Context
import android.content.SharedPreferences
import org.koin.mp.KoinPlatform.getKoin

private const val PREFERENCES_NAME = "purchases"

private class SharedPreferencesFirstPaywallStore(context: Context) : FirstPaywallStore {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun isShown(): Boolean = preferences.getBoolean(PAYWALL_FIRST_SHOWN_KEY, false)

    override fun markShown() {
        preferences.edit().putBoolean(PAYWALL_FIRST_SHOWN_KEY, true).apply()
    }
}

actual fun getPlatformFirstPaywallStore(): FirstPaywallStore =
    SharedPreferencesFirstPaywallStore(context = getKoin().get())
