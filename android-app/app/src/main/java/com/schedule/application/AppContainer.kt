package com.schedule.application

import android.content.Context
import com.schedule.application.data.local.AppDatabase
import com.schedule.application.data.repository.AdminRepository
import com.schedule.application.data.repository.ReminderRepository
import com.schedule.application.data.repository.ScheduleRepository
import com.schedule.application.data.repository.SettingsRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.get(context)

    val settingsRepository = SettingsRepository(context)
    val scheduleRepository = ScheduleRepository(
        settingsRepository = settingsRepository,
        lessonPriorityDao = database.lessonPriorityDao(),
    )
    val adminRepository = AdminRepository(settingsRepository = settingsRepository)
    val reminderRepository = ReminderRepository(
        settingsRepository = settingsRepository,
        scheduleRepository = scheduleRepository,
        reminderLogDao = database.reminderLogDao(),
    )
}
