package com.kturker.multiplatform.core.multilang.resource

/**
 * Every UI string of the app, one instance per language (see `resource/res`). The properties have
 * no default values on purpose: a language that misses a string does not compile. Add your
 * screens' strings here and in every language object.
 */
data class StringResourcesUiModel(
    val settingsTitle: String,
    val themeLight: String,
    val themeSystem: String,
    val themeDark: String,
    val languageSystem: String
)
