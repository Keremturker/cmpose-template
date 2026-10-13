package com.kturker.multiplatform.core.update

// A small key-value store on the device (SharedPreferences, NSUserDefaults): core/update works without
// core/database, which not every project has
interface UpdateCheckStore {
    // Null when the key was never written
    fun getLong(key: String): Long?

    fun putLong(
        key: String,
        value: Long
    )

    fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean

    fun putBoolean(
        key: String,
        value: Boolean
    )
}

expect fun getPlatformUpdateCheckStore(): UpdateCheckStore
