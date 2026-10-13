package com.kturker.multiplatform.core.database.theme

/**
 * The user's theme choice. DarkModeManager stores it by name, so reordering or adding entries
 * never changes a saved choice.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        /** The mode saved under [name]; an unknown, empty or missing value is [SYSTEM]. */
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
