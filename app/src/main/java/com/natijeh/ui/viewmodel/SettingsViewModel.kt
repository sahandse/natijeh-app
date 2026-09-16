package com.natijeh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.natijeh.data.settings.AppSettings
import com.natijeh.data.settings.SettingsStore
import com.natijeh.data.settings.ThemeMode
import com.natijeh.data.settings.CardDensity
import com.natijeh.data.settings.NotificationPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {
    val settings: StateFlow<AppSettings> = store.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { store.setThemeMode(mode) }
    fun setAlertsEnabled(enabled: Boolean) = viewModelScope.launch { store.setAlertsEnabled(enabled) }
    fun setGoalNotifications(enabled: Boolean) = viewModelScope.launch { store.setGoalNotifications(enabled) }
    fun setKeepScreenOnLive(enabled: Boolean) = viewModelScope.launch { store.setKeepScreenOnLive(enabled) }
    fun setOpenLiveTab(enabled: Boolean) = viewModelScope.launch { store.setOpenLiveTab(enabled) }
    fun setKickoffNotifications(enabled: Boolean) = viewModelScope.launch { store.setKickoffNotifications(enabled) }
    fun setRedCardNotifications(enabled: Boolean) = viewModelScope.launch { store.setRedCardNotifications(enabled) }
    fun setFullTimeNotifications(enabled: Boolean) = viewModelScope.launch { store.setFullTimeNotifications(enabled) }
    fun setPlayerNotifications(enabled: Boolean) = viewModelScope.launch { store.setPlayerNotifications(enabled) }
    fun completeOnboarding() = viewModelScope.launch { store.setOnboardingCompleted(true) }
    fun setCardDensity(density: CardDensity) = viewModelScope.launch { store.setCardDensity(density) }
    fun setNotificationPreset(preset: NotificationPreset) = viewModelScope.launch { store.setNotificationPreset(preset) }
    fun setDataSaver(enabled: Boolean) = viewModelScope.launch { store.setDataSaver(enabled) }
    fun setLineupNotifications(enabled: Boolean) = viewModelScope.launch { store.setLineupNotifications(enabled) }
    fun setSecondHalfNotifications(enabled: Boolean) = viewModelScope.launch { store.setSecondHalfNotifications(enabled) }

    class Factory(private val store: SettingsStore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(store) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
