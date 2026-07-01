package com.lifeos.app.feature.habit.domain

import java.time.Instant
import java.time.LocalDate

enum class HabitScheduleType { DAILY, WEEKLY, MONTHLY, CUSTOM }
enum class HabitDifficulty { EASY, MEDIUM, HARD }
enum class HabitImportance { LOW, MEDIUM, HIGH, CRITICAL }

data class Habit(
    val id: String,
    val title: String,
    val description: String = "",
    val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    val customDaysOfWeek: List<Int> = emptyList(),
    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    val importance: HabitImportance = HabitImportance.MEDIUM,
    val categoryId: String? = null,
    val isArchived: Boolean = false,
    val triggers: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
)

data class HabitCompletion(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val completed: Boolean,
    val note: String = "",
)

data class HabitStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
)

fun Habit.isScheduledOn(date: LocalDate): Boolean = when (scheduleType) {
    HabitScheduleType.DAILY, HabitScheduleType.WEEKLY, HabitScheduleType.MONTHLY -> true
    HabitScheduleType.CUSTOM -> customDaysOfWeek.contains(date.dayOfWeek.value)
}
