package com.kturker.multiplatform.core.multilang.language

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun getDeviceLanguageCode(): String {
    val preferredLanguages = NSLocale.preferredLanguages.firstOrNull() as? String
    return preferredLanguages?.substring(0, 2) ?: "en"
}
