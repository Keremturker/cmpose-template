package com.kturker.multiplatform.core.update

import platform.Foundation.NSUserDefaults

private class UserDefaultsUpdateCheckStore(private val defaults: NSUserDefaults) : UpdateCheckStore {

    override fun getLong(key: String): Long? =
        if (defaults.objectForKey(key) == null) null else defaults.integerForKey(key)

    override fun putLong(
        key: String,
        value: Long
    ) {
        defaults.setInteger(value, forKey = key)
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        defaults.setBool(value, forKey = key)
    }
}

actual fun getPlatformUpdateCheckStore(): UpdateCheckStore =
    UserDefaultsUpdateCheckStore(defaults = NSUserDefaults.standardUserDefaults)
