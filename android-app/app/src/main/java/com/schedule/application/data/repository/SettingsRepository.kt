package com.schedule.application.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.schedule.application.data.model.NotificationPriorityConfig
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.ThemeMode
import com.schedule.application.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val serverUrl = stringPreferencesKey("server_url")
        val readKey = stringPreferencesKey("read_key")
        val submitKey = stringPreferencesKey("submit_key")
        val selectedGroup = stringPreferencesKey("selected_group")
        val themeMode = stringPreferencesKey("theme_mode")

        fun priorityEnabled(priority: PriorityLevel): Preferences.Key<Boolean> =
            booleanPreferencesKey("notify_${priority.name.lowercase()}_enabled")

        fun priorityMinutes(priority: PriorityLevel): Preferences.Key<Int> =
            intPreferencesKey("notify_${priority.name.lowercase()}_minutes")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val notifications = PriorityLevel.entries.associateWith { priority ->
            NotificationPriorityConfig(
                enabled = preferences[Keys.priorityEnabled(priority)] ?: true,
                minutesBefore = preferences[Keys.priorityMinutes(priority)] ?: defaultMinutes(priority),
            )
        }

        UserSettings(
            serverUrl = preferences[Keys.serverUrl] ?: "https://example.onrender.com/",
            readKey = preferences[Keys.readKey] ?: "",
            submitKey = preferences[Keys.submitKey] ?: "",
            selectedGroup = preferences[Keys.selectedGroup] ?: "",
            themeMode = parseTheme(preferences[Keys.themeMode]),
            notificationConfig = notifications,
        )
    }

    suspend fun updateServerUrl(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.serverUrl] = value.trim()
        }
    }

    suspend fun updateReadKey(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.readKey] = value.trim()
        }
    }

    suspend fun updateSubmitKey(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.submitKey] = value.trim()
        }
    }

    suspend fun updateSelectedGroup(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.selectedGroup] = value.trim()
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.themeMode] = mode.name
        }
    }

    suspend fun updatePriorityNotification(priority: PriorityLevel, enabled: Boolean, minutesBefore: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.priorityEnabled(priority)] = enabled
            prefs[Keys.priorityMinutes(priority)] = minutesBefore.coerceIn(0, 180)
        }
    }

    private fun parseTheme(raw: String?): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    private fun defaultMinutes(priority: PriorityLevel): Int {
        return when (priority) {
            PriorityLevel.RED -> 45
            PriorityLevel.ORANGE -> 35
            PriorityLevel.YELLOW -> 30
            PriorityLevel.DARK_GREEN -> 20
            PriorityLevel.LIGHT_GREEN -> 10
        }
    }
}
