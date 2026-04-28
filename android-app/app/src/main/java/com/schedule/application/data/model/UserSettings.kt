package com.schedule.application.data.model

data class UserSettings(
    val serverUrl: String = "https://example.onrender.com/",
    val readKey: String = "",
    val submitKey: String = "",
    val selectedGroup: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationConfig: Map<PriorityLevel, NotificationPriorityConfig> = PriorityLevel.entries.associateWith {
        NotificationPriorityConfig()
    },
)
