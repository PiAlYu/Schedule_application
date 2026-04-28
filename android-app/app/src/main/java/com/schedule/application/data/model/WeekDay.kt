package com.schedule.application.data.model

enum class WeekDay(val isoDay: Int, val shortTitle: String) {
    MONDAY(1, "Mon"),
    TUESDAY(2, "Tue"),
    WEDNESDAY(3, "Wed"),
    THURSDAY(4, "Thu"),
    FRIDAY(5, "Fri"),
    SATURDAY(6, "Sat"),
    SUNDAY(7, "Sun");

    companion object {
        fun fromIsoDay(isoDay: Int): WeekDay = entries.firstOrNull { it.isoDay == isoDay } ?: MONDAY
    }
}
