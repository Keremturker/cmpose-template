package com.kturker.multiplatform.core.review

// A small key-value store on the device (SharedPreferences, NSUserDefaults): core/review works without
// core/database, which not every project has
interface ReviewCounterStore {
    fun getInt(
        key: String,
        default: Int
    ): Int

    fun putInt(
        key: String,
        value: Int
    )
}

expect fun getPlatformReviewCounterStore(): ReviewCounterStore
