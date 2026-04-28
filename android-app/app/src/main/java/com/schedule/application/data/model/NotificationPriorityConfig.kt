package com.schedule.application.data.model

data class NotificationPriorityConfig(
    val enabled: Boolean = true,
    val minutesBefore: Int = 30,
)
