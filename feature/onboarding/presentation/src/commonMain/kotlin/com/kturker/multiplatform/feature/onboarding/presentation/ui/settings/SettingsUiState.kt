package com.kturker.multiplatform.feature.onboarding.presentation.ui.settings

import com.kturker.multiplatform.core.database.theme.ThemeMode
import com.kturker.multiplatform.core.multilang.language.AppLanguage

internal data class SettingsUiState(
    val selectedMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val versionText: String = "",
    val privacyPolicyUrl: String? = null,
    val termsUrl: String? = null
)
