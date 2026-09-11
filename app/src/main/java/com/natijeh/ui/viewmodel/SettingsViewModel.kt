package com.natijeh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.natijeh.data.settings.AppSettings
import com.natijeh.data.settings.SettingsStore
import com.natijeh.data.settings.ThemeMode
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
    fun setGoalNotifications(enabled: Boolean) = viewModelScope.launch { store.setGoalNotifications(enabled) }
    fun setKeepScreenOnLive(enabled: Boolean) = viewModelScope.launch { store.setKeepScreenOnLive(enabled) }
    fun setOpenLiveTab(enabled: Boolean) = viewModelScope.launch { store.setOpenLiveTab(enabled) }

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
