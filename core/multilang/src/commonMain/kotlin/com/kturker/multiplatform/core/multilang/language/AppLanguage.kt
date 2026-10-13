package com.kturker.multiplatform.core.multilang.language

// displayName is the language's own name; SYSTEM has none, the UI shows the localized languageSystem string.
enum class AppLanguage(val code: String, val displayName: String?) {
    SYSTEM(code = "SYSTEM", null),
    EN(code = "en", "English"),
    TR(code = "tr", "Türkçe"),
    ES(code = "es", "Español"),
    IT(code = "it", "Italiano"),
    FR(code = "fr", "Français");

    companion object Companion {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}
