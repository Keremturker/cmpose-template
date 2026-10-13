package com.kturker.multiplatform.core.review

import platform.Foundation.NSUserDefaults

private class UserDefaultsReviewCounterStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : ReviewCounterStore {

    override fun getInt(
        key: String,
        default: Int
    ): Int =
        if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key).toInt()

    override fun putInt(
        key: String,
        value: Int
    ) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}

actual fun getPlatformReviewCounterStore(): ReviewCounterStore = UserDefaultsReviewCounterStore()
