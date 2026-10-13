package com.kturker.multiplatform.core.database.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DarkModeManagerImplTest {

    private val themeModeKey = stringPreferencesKey("themeMode")

    @Test
    fun theChoiceIsStoredByName() = runTest {
        val dataStore = InMemoryPreferences()

        DarkModeManagerImpl(dataStore).setThemeMode(ThemeMode.DARK)

        assertEquals("DARK", dataStore.data.first()[themeModeKey])
    }

    @Test
    fun theStoredChoiceIsReadBack() = runTest {
        val manager = DarkModeManagerImpl(InMemoryPreferences())

        assertEquals(ThemeMode.SYSTEM, manager.getThemeMode().first(), "nothing stored yet")
        manager.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, manager.getThemeMode().first())
    }

    @Test
    fun anUnknownStoredNameReadsAsSystem() = runTest {
        val dataStore = InMemoryPreferences()
        dataStore.edit { it[themeModeKey] = "SEPIA" }

        assertEquals(ThemeMode.SYSTEM, DarkModeManagerImpl(dataStore).getThemeMode().first())
    }
}

private class InMemoryPreferences : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}
