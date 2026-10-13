package com.kturker.multiplatform.core.multilang.language

import com.kturker.multiplatform.core.multilang.resource.StringResourcesUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LanguageManager {
    val currentResources: StateFlow<StringResourcesUiModel>

    fun getCurrentLanguageFlow(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setSystemLanguage()
}
