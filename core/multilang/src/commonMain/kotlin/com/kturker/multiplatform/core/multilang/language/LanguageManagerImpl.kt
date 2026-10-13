package com.kturker.multiplatform.core.multilang.language

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kturker.multiplatform.core.multilang.resource.StringResourcesUiModel
import com.kturker.multiplatform.core.multilang.resource.res.fallbackResources
import com.kturker.multiplatform.core.multilang.resource.res.languageResources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [LanguageManager::class])
class LanguageManagerImpl(@Provided private val dataStore: DataStore<Preferences>) : LanguageManager {

    private val selectedLanguageKey = stringPreferencesKey("selectedLanguage")

    // Starts from the device language, so the first frames of the common SYSTEM case are already
    // right; setSystemLanguage() then applies a saved choice.
    private val _currentResources = MutableStateFlow(resourcesFor(AppLanguage.SYSTEM))
    override val currentResources: StateFlow<StringResourcesUiModel> =
        _currentResources.asStateFlow()

    override fun getCurrentLanguageFlow(): Flow<AppLanguage> = dataStore.data.map { prefs ->
        val storedCode = prefs[selectedLanguageKey] ?: AppLanguage.SYSTEM.code
        AppLanguage.fromCode(storedCode)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[selectedLanguageKey] = language.code
        }

        _currentResources.update { resourcesFor(language) }
    }

    override suspend fun setSystemLanguage() {
        val prefs = dataStore.data.firstOrNull()
        val selectedLanguage = AppLanguage.fromCode(code = prefs?.get(selectedLanguageKey))

        _currentResources.update { resourcesFor(selectedLanguage) }
    }

    // SYSTEM follows the device. A device language without a translation resolves to SYSTEM again,
    // which has no resources: fall back to English instead of showing empty strings.
    private fun resourcesFor(language: AppLanguage): StringResourcesUiModel {
        val active = if (language == AppLanguage.SYSTEM) {
            AppLanguage.fromCode(getDeviceLanguageCode())
        } else {
            language
        }
        return languageResources[active] ?: fallbackResources
    }
}
