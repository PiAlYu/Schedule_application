package com.schedule.application.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.schedule.application.R
import com.schedule.application.ScheduleApplication
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.PriorityLevel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ScheduleApplication
        val repository = app.appContainer.reminderRepository

        val payload = repository.getTodayLessons().getOrElse {
            return Result.retry()
        }

        val settings = payload.first
        val lessons = payload.second

        repository.cleanupOldReminders()
        createChannel(applicationContext)

        val now = LocalDateTime.now()
        val today = LocalDate.now().toString()

        lessons.forEach { lesson ->
            val config = settings.notificationConfig[lesson.priority] ?: return@forEach
            if (!config.enabled) return@forEach

            val startTime = parseTime(lesson.startTime) ?: return@forEach
            val lessonStart = LocalDateTime.of(LocalDate.now(), startTime)
            val reminderMoment = lessonStart.minusMinutes(config.minutesBefore.toLong())

            val isInReminderWindow = now.isAfter(reminderMoment) && now.isBefore(lessonStart)
            if (!isInReminderWindow) return@forEach

            val reminderId = "$today|${lesson.groupName}|${lesson.dayOfWeek}|${lesson.pairNumber}|${config.minutesBefore}"
            if (repository.isReminderAlreadySent(reminderId)) return@forEach

            postNotification(lesson)
            repository.markReminderSent(reminderId)
        }

        return Result.success()
    }

    private fun parseTime(raw: String): LocalTime? {
        return try {
            LocalTime.parse(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun postNotification(lesson: Lesson) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val title = "Upcoming lesson #${lesson.pairNumber}"
        val text = "${lesson.startTime}-${lesson.endTime} ${lesson.subject} (${lesson.teacher})"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(mapPriority(lesson.priority))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(lesson.id, notification)
    }

    private fun mapPriority(priority: PriorityLevel): Int {
        return when (priority) {
            PriorityLevel.RED -> NotificationCompat.PRIORITY_MAX
            PriorityLevel.ORANGE -> NotificationCompat.PRIORITY_HIGH
            PriorityLevel.YELLOW -> NotificationCompat.PRIORITY_DEFAULT
            PriorityLevel.DARK_GREEN -> NotificationCompat.PRIORITY_LOW
            PriorityLevel.LIGHT_GREEN -> NotificationCompat.PRIORITY_MIN
        }
    }

    companion object {
        private const val CHANNEL_ID = "lesson_reminders"
        private const val WORK_NAME = "lesson_reminder_worker"

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
