package com.kturker.multiplatform.core.database.theme

import kotlinx.coroutines.flow.Flow

/** The user's Light/Dark/System choice, kept in DataStore; shared/.../AppTheme.kt applies it. */
interface DarkModeManager {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
