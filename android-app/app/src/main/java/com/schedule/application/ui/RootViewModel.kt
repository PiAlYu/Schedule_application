package com.schedule.application.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schedule.application.data.model.UserSettings
import com.schedule.application.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RootUiState())
    val state: StateFlow<RootUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
    }
}

data class RootUiState(
    val settings: UserSettings = UserSettings(),
)
