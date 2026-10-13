package com.kturker.multiplatform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * The app's theme: Material's light or dark colors, following the system setting. Put your own
 * colors, typography and shapes here (lightColorScheme(primary = ...)); do not add a second
 * MaterialTheme elsewhere.
 */
@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}
