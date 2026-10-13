package com.kturker.multiplatform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kturker.multiplatform.core.database.theme.DarkModeManager
import com.kturker.multiplatform.core.database.theme.ThemeMode
import com.kturker.multiplatform.core.designsystem.theme.KtTheme
import org.koin.compose.koinInject

/**
 * The app's theme: the user's Light/Dark/System choice (DarkModeManager, saved in DataStore)
 * applied through KtTheme. Until the saved choice loads, the app follows the system, so a
 * dark-mode device never starts on a light frame.
 */
@Composable
internal fun AppTheme(
    darkModeManager: DarkModeManager = koinInject(),
    content: @Composable () -> Unit
) {
    val themeModeFlow = remember(darkModeManager) { darkModeManager.getThemeMode() }
    val themeMode by themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    KtTheme(darkTheme = darkTheme, content = content)
}
