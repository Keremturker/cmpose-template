package com.kturker.multiplatform.core.review

import android.content.Context
import android.content.SharedPreferences
import org.koin.mp.KoinPlatform.getKoin

private const val PREFERENCES_NAME = "in_app_review"

internal class SharedPreferencesReviewCounterStore(context: Context) : ReviewCounterStore {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getInt(
        key: String,
        default: Int
    ): Int = preferences.getInt(key, default)

    override fun putInt(
        key: String,
        value: Int
    ) {
        preferences.edit().putInt(key, value).apply()
    }
}

actual fun getPlatformReviewCounterStore(): ReviewCounterStore =
    SharedPreferencesReviewCounterStore(context = getKoin().get())
