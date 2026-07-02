package com.lifeos.app.feature.task.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasksForDate(date: LocalDate): Flow<List<Task>>
    fun observeTasksInRange(start: LocalDate, end: LocalDate): Flow<List<Task>>
    suspend fun getTask(id: String): Task?
    suspend fun upsertTask(task: Task)
    suspend fun deleteTask(id: String)
    suspend fun setCompleted(id: String, completed: Boolean)
    suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int>
    suspend fun totalCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int>
    fun observeCompletionsByDay(start: LocalDate, end: LocalDate): Flow<Map<Long, Int>>
    fun observeTotalByDay(start: LocalDate, end: LocalDate): Flow<Map<Long, Int>>
    suspend fun getTriggerFailureCounts(): Map<String, Int>
    fun observeUnscheduledUpcoming(limit: Int = 6): Flow<List<Task>>
}
