package com.schedule.application.data.repository

import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.network.AdminLoginRequest
import com.schedule.application.data.network.ApiFactory
import com.schedule.application.data.network.ScheduleEntryDto
import com.schedule.application.data.network.ScheduleUpdateRequest
import com.schedule.application.data.network.ScheduleUpsertRequest
import kotlinx.coroutines.flow.first

class AdminRepository(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        val token = api.adminLogin(AdminLoginRequest(username = username, password = password))
        token.access_token
    }

    suspend fun listSchedules(token: String): Result<List<Lesson>> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        val items = api.listAdminSchedules(
            bearerToken = bearer(token),
            groupName = settings.selectedGroup.ifBlank { null },
        )
        items.map(::toLesson)
    }

    suspend fun upsertSchedule(
        token: String,
        groupName: String,
        dayOfWeek: Int,
        pairNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacher: String,
    ): Result<Lesson> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        val result = api.upsertSchedule(
            bearerToken = bearer(token),
            request = ScheduleUpsertRequest(
                group_name = groupName.ifBlank { settings.selectedGroup },
                day_of_week = dayOfWeek,
                pair_number = pairNumber,
                start_time = startTime,
                end_time = endTime,
                subject = subject,
                teacher = teacher,
            ),
        )
        toLesson(result)
    }

    suspend fun updateSchedule(
        token: String,
        entryId: Int,
        groupName: String,
        dayOfWeek: Int,
        pairNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacher: String,
    ): Result<Lesson> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        val result = api.updateSchedule(
            bearerToken = bearer(token),
            entryId = entryId,
            request = ScheduleUpdateRequest(
                group_name = groupName.ifBlank { settings.selectedGroup },
                day_of_week = dayOfWeek,
                pair_number = pairNumber,
                start_time = startTime,
                end_time = endTime,
                subject = subject,
                teacher = teacher,
            ),
        )
        toLesson(result)
    }

    suspend fun deleteSchedule(token: String, entryId: Int): Result<Unit> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        api.deleteSchedule(bearerToken = bearer(token), entryId = entryId)
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun toLesson(dto: ScheduleEntryDto): Lesson {
        return Lesson(
            id = dto.id,
            groupName = dto.group_name,
            dayOfWeek = dto.day_of_week,
            pairNumber = dto.pair_number,
            startTime = dto.start_time,
            endTime = dto.end_time,
            subject = dto.subject,
            teacher = dto.teacher,
            priority = PriorityLevel.YELLOW,
        )
    }
}
