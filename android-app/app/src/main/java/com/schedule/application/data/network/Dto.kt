package com.schedule.application.data.network

data class ScheduleEntryDto(
    val id: Int,
    val group_name: String,
    val day_of_week: Int,
    val pair_number: Int,
    val start_time: String,
    val end_time: String,
    val subject: String,
    val teacher: String,
)

data class DayScheduleDto(
    val group_name: String,
    val day_of_week: Int,
    val entries: List<ScheduleEntryDto>,
)

data class WeekScheduleDto(
    val group_name: String,
    val days: List<DayScheduleDto>,
)

data class ProposalCreateRequest(
    val group_name: String,
    val day_of_week: Int,
    val pair_number: Int,
    val start_time: String,
    val end_time: String,
    val subject: String,
    val teacher: String,
    val submitted_by: String,
)

data class ProposalReadDto(
    val id: Int,
    val group_name: String,
    val day_of_week: Int,
    val pair_number: Int,
    val start_time: String,
    val end_time: String,
    val subject: String,
    val teacher: String,
    val submitted_by: String,
    val reviewer_comment: String?,
    val status: String,
)

data class AdminLoginRequest(
    val username: String,
    val password: String,
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
)

data class ScheduleUpsertRequest(
    val group_name: String,
    val day_of_week: Int,
    val pair_number: Int,
    val start_time: String,
    val end_time: String,
    val subject: String,
    val teacher: String,
)

data class ScheduleUpdateRequest(
    val group_name: String? = null,
    val day_of_week: Int? = null,
    val pair_number: Int? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val subject: String? = null,
    val teacher: String? = null,
)
