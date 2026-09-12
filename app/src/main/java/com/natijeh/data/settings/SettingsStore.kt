package com.natijeh.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "natijeh_settings")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> systemDark
    }

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.name == value } ?: DARK
        }
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val alertsEnabled: Boolean = true,
    val goalNotifications: Boolean = true,
    val keepScreenOnLive: Boolean = true,
    val openLiveTab: Boolean = true,
    val kickoffNotifications: Boolean = true,
    val redCardNotifications: Boolean = true,
    val fullTimeNotifications: Boolean = true,
    val playerNotifications: Boolean = true
)

class SettingsStore(private val context: Context) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.fromStorage(prefs[THEME_MODE]),
            alertsEnabled = prefs[ALERTS_ENABLED] ?: true,
            goalNotifications = prefs[GOAL_NOTIFICATIONS] ?: true,
            keepScreenOnLive = prefs[KEEP_SCREEN_ON] ?: true,
            openLiveTab = prefs[OPEN_LIVE_TAB] ?: true,
            kickoffNotifications = prefs[KICKOFF_NOTIFICATIONS] ?: true,
            redCardNotifications = prefs[RED_CARD_NOTIFICATIONS] ?: true,
            fullTimeNotifications = prefs[FULL_TIME_NOTIFICATIONS] ?: true,
            playerNotifications = prefs[PLAYER_NOTIFICATIONS] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) = context.dataStore.edit { it[ALERTS_ENABLED] = enabled }

    suspend fun setGoalNotifications(enabled: Boolean) {
        context.dataStore.edit { it[GOAL_NOTIFICATIONS] = enabled }
    }

    suspend fun setKeepScreenOnLive(enabled: Boolean) {
        context.dataStore.edit { it[KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setOpenLiveTab(enabled: Boolean) {
        context.dataStore.edit { it[OPEN_LIVE_TAB] = enabled }
    }

    suspend fun setKickoffNotifications(enabled: Boolean) = context.dataStore.edit { it[KICKOFF_NOTIFICATIONS] = enabled }
    suspend fun setRedCardNotifications(enabled: Boolean) = context.dataStore.edit { it[RED_CARD_NOTIFICATIONS] = enabled }
    suspend fun setFullTimeNotifications(enabled: Boolean) = context.dataStore.edit { it[FULL_TIME_NOTIFICATIONS] = enabled }
    suspend fun setPlayerNotifications(enabled: Boolean) = context.dataStore.edit { it[PLAYER_NOTIFICATIONS] = enabled }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        private val GOAL_NOTIFICATIONS = booleanPreferencesKey("goal_notifications")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val OPEN_LIVE_TAB = booleanPreferencesKey("open_live_tab")
        private val KICKOFF_NOTIFICATIONS = booleanPreferencesKey("kickoff_notifications")
        private val RED_CARD_NOTIFICATIONS = booleanPreferencesKey("red_card_notifications")
        private val FULL_TIME_NOTIFICATIONS = booleanPreferencesKey("full_time_notifications")
        private val PLAYER_NOTIFICATIONS = booleanPreferencesKey("player_notifications")
    }
}
