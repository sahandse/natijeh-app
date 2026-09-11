package com.natijeh.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun fromStorage_readsKnownValues() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromStorage("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("SYSTEM"))
    }

    @Test
    fun fromStorage_defaultsUnknownToDark() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage(""))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("night"))
    }

    @Test
    fun isDark_followsModeAndSystem() {
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }
}
