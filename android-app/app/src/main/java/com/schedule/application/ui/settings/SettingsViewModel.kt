package com.schedule.application.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.ThemeMode
import com.schedule.application.data.model.UserSettings
import com.schedule.application.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
    }

    fun updateServerUrl(value: String) {
        viewModelScope.launch {
            settingsRepository.updateServerUrl(value)
        }
    }

    fun updateReadKey(value: String) {
        viewModelScope.launch {
            settingsRepository.updateReadKey(value)
        }
    }

    fun updateSubmitKey(value: String) {
        viewModelScope.launch {
            settingsRepository.updateSubmitKey(value)
        }
    }

    fun updateGroup(value: String) {
        viewModelScope.launch {
            settingsRepository.updateSelectedGroup(value)
        }
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updatePriorityNotification(priority: PriorityLevel, enabled: Boolean, minutesBefore: Int) {
        viewModelScope.launch {
            settingsRepository.updatePriorityNotification(priority, enabled, minutesBefore)
        }
    }
}

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
)
