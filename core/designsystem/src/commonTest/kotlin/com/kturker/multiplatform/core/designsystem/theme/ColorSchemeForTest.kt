package com.kturker.multiplatform.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorSchemeForTest {

    @Test
    fun lightSchemeTakesItsRolesFromTheLightPalette() {
        assertRoles(OnLightColorsPalette, colorSchemeFor(OnLightColorsPalette, darkTheme = false), lightColorScheme())
    }

    @Test
    fun darkSchemeTakesItsRolesFromTheDarkPalette() {
        assertRoles(OnDarkColorsPalette, colorSchemeFor(OnDarkColorsPalette, darkTheme = true), darkColorScheme())
    }

    private fun assertRoles(palette: CustomColorsPalette, scheme: ColorScheme, baseline: ColorScheme) {
        assertEquals(palette.backgroundColor, scheme.background, "background")
        assertEquals(palette.backgroundColor, scheme.surface, "surface")
        assertEquals(palette.textBlack, scheme.onBackground, "onBackground")
        assertEquals(palette.textBlack, scheme.onSurface, "onSurface")
        assertEquals(palette.textGray, scheme.onSurfaceVariant, "onSurfaceVariant")
        assertEquals(palette.tintBlack, scheme.primary, "primary")
        assertEquals(palette.backgroundColor, scheme.onPrimary, "onPrimary")
        assertEquals(palette.textFieldUnFocusedBorder, scheme.outline, "outline")
        // Roles the palette has no colour for keep the light or dark baseline
        assertEquals(baseline.error, scheme.error, "error")
        assertEquals(baseline.secondary, scheme.secondary, "secondary")
    }
}
