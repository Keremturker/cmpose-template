package com.kturker.multiplatform.core.update

// The update UI's texts; the defaults are English. The showcase passes its translations.
data class UpdateTexts(
    val availableTitle: String = "Update available",
    val availableMessage: String = "A new version of the app is available.",
    val update: String = "Update",
    val later: String = "Later",
    val requiredTitle: String = "Update required",
    val requiredMessage: String = "This version is no longer supported. Update the app to continue.",
    val downloaded: String = "Update downloaded",
    val restart: String = "Restart"
)
