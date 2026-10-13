package com.kturker.multiplatform.core.database.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeModeTest {

    @Test
    fun everyModeReadsBackByItsName() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromName(mode.name))
        }
    }

    @Test
    fun anUnknownOrMissingNameIsSystem() {
        listOf(null, "", "SEPIA", "dark", "1").forEach { stored ->
            assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(stored), "stored=$stored")
        }
    }
}
