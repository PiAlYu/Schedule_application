package com.schedule.application.data.model

data class Lesson(
    val id: Int,
    val groupName: String,
    val dayOfWeek: Int,
    val pairNumber: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacher: String,
    val priority: PriorityLevel,
)
