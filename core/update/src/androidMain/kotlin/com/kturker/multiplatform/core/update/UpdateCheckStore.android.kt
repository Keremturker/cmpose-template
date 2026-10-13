package com.kturker.multiplatform.core.update

import android.content.Context
import android.content.SharedPreferences
import org.koin.mp.KoinPlatform.getKoin

private const val PREFERENCES_NAME = "app_update"

internal class SharedPreferencesUpdateCheckStore(context: Context) : UpdateCheckStore {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getLong(key: String): Long? =
        if (preferences.contains(key)) preferences.getLong(key, 0L) else null

    override fun putLong(
        key: String,
        value: Long
    ) {
        preferences.edit().putLong(key, value).apply()
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = preferences.getBoolean(key, default)

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        preferences.edit().putBoolean(key, value).apply()
    }
}

actual fun getPlatformUpdateCheckStore(): UpdateCheckStore =
    SharedPreferencesUpdateCheckStore(context = getKoin().get())
