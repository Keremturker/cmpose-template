package com.kturker.multiplatform.core.about

import platform.Foundation.NSBundle

actual fun currentAppVersion(): AppVersion {
    val info = NSBundle.mainBundle.infoDictionary.orEmpty()
    return AppVersion(
        name = info["CFBundleShortVersionString"] as? String ?: "",
        code = info["CFBundleVersion"] as? String ?: ""
    )
}
