package com.schedule.application.data.model

data class DaySchedule(
    val groupName: String,
    val dayOfWeek: Int,
    val lessons: List<Lesson>,
)

data class WeekSchedule(
    val groupName: String,
    val days: List<DaySchedule>,
)
