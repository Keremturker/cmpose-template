package com.kturker.multiplatform.core.about

// versionName and versionCode on Android, CFBundleShortVersionString and CFBundleVersion on iOS
data class AppVersion(val name: String, val code: String) {
    val displayText: String
        get() = if (code.isBlank()) name else "$name ($code)"
}

expect fun currentAppVersion(): AppVersion
