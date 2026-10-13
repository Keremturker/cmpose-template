package com.kturker.multiplatform.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * The app's theme: the palette for [darkTheme] as [LocalColorsPalette], and a Material color
 * scheme derived from it, so Material components without explicit colors (Text, Button,
 * CircularProgressIndicator, Scaffold) follow light and dark mode too. Wrap the app in it once
 * (shared/.../AppTheme.kt does); add typography and shapes to the MaterialTheme call here.
 */
@Composable
fun KtTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) OnDarkColorsPalette else OnLightColorsPalette
    CompositionLocalProvider(LocalColorsPalette provides palette) {
        MaterialTheme(colorScheme = colorSchemeFor(palette, darkTheme), content = content)
    }
}

/** The palette's colors in their Material roles; every other role keeps the light or dark baseline. */
internal fun colorSchemeFor(
    palette: CustomColorsPalette,
    darkTheme: Boolean
): ColorScheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
    primary = palette.tintBlack,
    onPrimary = palette.backgroundColor,
    background = palette.backgroundColor,
    onBackground = palette.textBlack,
    surface = palette.backgroundColor,
    onSurface = palette.textBlack,
    onSurfaceVariant = palette.textGray,
    outline = palette.textFieldUnFocusedBorder
)
