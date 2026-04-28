package com.schedule.application.data.repository

import com.schedule.application.data.local.ReminderLogDao
import com.schedule.application.data.local.ReminderLogEntity
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.UserSettings
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderRepository(
    private val settingsRepository: SettingsRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reminderLogDao: ReminderLogDao,
) {
    suspend fun getTodayLessons(): Result<Pair<UserSettings, List<Lesson>>> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val schedule = scheduleRepository.fetchWeekSchedule().getOrThrow()
        val todayIso = LocalDate.now().dayOfWeek.value
        val lessons = schedule.days.firstOrNull { it.dayOfWeek == todayIso }?.lessons.orEmpty()
        settings to lessons
    }

    suspend fun isReminderAlreadySent(reminderId: String): Boolean = reminderLogDao.exists(reminderId)

    suspend fun markReminderSent(reminderId: String) {
        reminderLogDao.insert(
            ReminderLogEntity(
                id = reminderId,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun cleanupOldReminders() {
        val threeDaysMillis = 3L * 24L * 60L * 60L * 1000L
        reminderLogDao.deleteOlderThan(System.currentTimeMillis() - threeDaysMillis)
    }
}
