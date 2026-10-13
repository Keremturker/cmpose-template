package com.kturker.multiplatform.core.database.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [DarkModeManager::class])
class DarkModeManagerImpl(@Provided private val dataStore: DataStore<Preferences>) : DarkModeManager {

    // Stored by name: reordering or adding ThemeMode entries never changes a saved choice
    private val themeModeKey = stringPreferencesKey("themeMode")

    override fun getThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { preferences -> ThemeMode.fromName(preferences[themeModeKey]) }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }
}
