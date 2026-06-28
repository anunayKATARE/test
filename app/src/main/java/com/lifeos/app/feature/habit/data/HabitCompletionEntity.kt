package com.lifeos.app.feature.habit.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lifeos.app.feature.habit.domain.HabitCompletion
import java.time.LocalDate

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "date"], unique = true)],
)
data class HabitCompletionEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: LocalDate,
    val completed: Boolean,
    val note: String,
)

fun HabitCompletionEntity.toDomain() = HabitCompletion(
    id = id,
    habitId = habitId,
    date = date,
    completed = completed,
    note = note,
)

fun HabitCompletion.toEntity() = HabitCompletionEntity(
    id = id,
    habitId = habitId,
    date = date,
    completed = completed,
    note = note,
)
