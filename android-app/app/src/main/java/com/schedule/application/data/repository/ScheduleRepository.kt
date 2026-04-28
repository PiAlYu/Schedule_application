package com.schedule.application.data.repository

import com.schedule.application.data.local.LessonPriorityDao
import com.schedule.application.data.local.LessonPriorityEntity
import com.schedule.application.data.model.DaySchedule
import com.schedule.application.data.model.Lesson
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.UserSettings
import com.schedule.application.data.model.WeekSchedule
import com.schedule.application.data.network.ApiFactory
import com.schedule.application.data.network.ProposalCreateRequest
import com.schedule.application.data.network.ScheduleEntryDto
import com.schedule.application.data.network.WeekScheduleDto
import kotlinx.coroutines.flow.first

class ScheduleRepository(
    private val settingsRepository: SettingsRepository,
    private val lessonPriorityDao: LessonPriorityDao,
) {
    suspend fun fetchAvailableGroups(): Result<List<String>> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        val api = ApiFactory.create(settings.serverUrl)
        api.getGroups(readKey = settings.readKey)
    }

    suspend fun fetchWeekSchedule(): Result<WeekSchedule> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        require(settings.selectedGroup.isNotBlank()) { "Select a student group in settings" }

        val api = ApiFactory.create(settings.serverUrl)
        val dto = api.getWeekSchedule(readKey = settings.readKey, groupName = settings.selectedGroup)
        mapWeekSchedule(dto = dto, settings = settings)
    }

    suspend fun submitProposal(
        dayOfWeek: Int,
        pairNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacher: String,
        submittedBy: String,
    ): Result<Unit> = runCatching {
        val settings = settingsRepository.settingsFlow.first()
        require(settings.selectedGroup.isNotBlank()) { "Select a student group in settings" }

        val api = ApiFactory.create(settings.serverUrl)
        api.createProposal(
            submitKey = settings.submitKey,
            request = ProposalCreateRequest(
                group_name = settings.selectedGroup,
                day_of_week = dayOfWeek,
                pair_number = pairNumber,
                start_time = startTime,
                end_time = endTime,
                subject = subject,
                teacher = teacher,
                submitted_by = submittedBy.ifBlank { "anonymous" },
            ),
        )
    }

    suspend fun updatePriority(groupName: String, dayOfWeek: Int, pairNumber: Int, priority: PriorityLevel) {
        lessonPriorityDao.upsert(
            LessonPriorityEntity(
                groupName = groupName,
                dayOfWeek = dayOfWeek,
                pairNumber = pairNumber,
                priorityValue = priority.storageValue,
            )
        )
    }

    private suspend fun mapWeekSchedule(dto: WeekScheduleDto, settings: UserSettings): WeekSchedule {
        val priorities = lessonPriorityDao.getAllForGroup(settings.selectedGroup)
            .associateBy { Triple(it.groupName, it.dayOfWeek, it.pairNumber) }

        val mappedDays = dto.days.map { day ->
            val lessons = day.entries.map { entry ->
                mapLesson(entry, priorities)
            }
            DaySchedule(
                groupName = day.group_name,
                dayOfWeek = day.day_of_week,
                lessons = lessons,
            )
        }

        return WeekSchedule(groupName = dto.group_name, days = mappedDays)
    }

    private fun mapLesson(
        entry: ScheduleEntryDto,
        priorities: Map<Triple<String, Int, Int>, LessonPriorityEntity>,
    ): Lesson {
        val key = Triple(entry.group_name, entry.day_of_week, entry.pair_number)
        val priority = priorities[key]?.priorityValue?.let(PriorityLevel::fromStorageValue) ?: PriorityLevel.YELLOW

        return Lesson(
            id = entry.id,
            groupName = entry.group_name,
            dayOfWeek = entry.day_of_week,
            pairNumber = entry.pair_number,
            startTime = entry.start_time,
            endTime = entry.end_time,
            subject = entry.subject,
            teacher = entry.teacher,
            priority = priority,
        )
    }
}
