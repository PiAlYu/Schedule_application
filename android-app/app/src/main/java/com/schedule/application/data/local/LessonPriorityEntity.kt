package com.schedule.application.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "lesson_priorities",
    primaryKeys = ["group_name", "day_of_week", "pair_number"],
)
data class LessonPriorityEntity(
    @ColumnInfo(name = "group_name") val groupName: String,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "pair_number") val pairNumber: Int,
    @ColumnInfo(name = "priority_value") val priorityValue: Int,
)
