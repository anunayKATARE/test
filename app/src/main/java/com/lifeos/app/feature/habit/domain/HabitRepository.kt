package com.lifeos.app.feature.habit.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeAllHabits(): Flow<List<Habit>>
    suspend fun getHabit(id: String): Habit?
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(id: String)
    suspend fun setArchived(id: String, archived: Boolean)

    fun observeCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>>
    fun observeCompletionsOn(date: LocalDate): Flow<List<HabitCompletion>>
    suspend fun getCompletionsBetween(habitId: String, start: LocalDate, end: LocalDate): List<HabitCompletion>
    suspend fun setCompletion(habitId: String, date: LocalDate, completed: Boolean, note: String = "")
    suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int>
    fun observeCompletionsByDay(start: LocalDate, end: LocalDate): Flow<Map<Long, Int>>
}
