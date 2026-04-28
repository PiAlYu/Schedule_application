package com.schedule.application.data.model

enum class PriorityLevel(val storageValue: Int, val title: String) {
    RED(0, "Highest"),
    ORANGE(1, "High"),
    YELLOW(2, "Medium"),
    DARK_GREEN(3, "Low"),
    LIGHT_GREEN(4, "Minimal");

    companion object {
        fun fromStorageValue(value: Int): PriorityLevel = entries.firstOrNull { it.storageValue == value } ?: YELLOW
    }
}
