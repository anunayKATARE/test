package com.lifeos.app.feature.habit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import java.time.Instant

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val scheduleType: HabitScheduleType,
    val customDaysOfWeek: List<Int>,
    val difficulty: HabitDifficulty,
    val importance: HabitImportance,
    val categoryId: String?,
    val isArchived: Boolean,
    val triggers: List<String> = emptyList(),
    val createdAt: Instant,
    val profileId: String? = null,
)

fun HabitEntity.toDomain() = Habit(
    id = id,
    title = title,
    description = description,
    scheduleType = scheduleType,
    customDaysOfWeek = customDaysOfWeek,
    difficulty = difficulty,
    importance = importance,
    categoryId = categoryId,
    isArchived = isArchived,
    triggers = triggers,
    createdAt = createdAt,
)

fun Habit.toEntity() = HabitEntity(
    id = id,
    title = title,
    description = description,
    scheduleType = scheduleType,
    customDaysOfWeek = customDaysOfWeek,
    difficulty = difficulty,
    importance = importance,
    categoryId = categoryId,
    isArchived = isArchived,
    triggers = triggers,
    createdAt = createdAt,
)
