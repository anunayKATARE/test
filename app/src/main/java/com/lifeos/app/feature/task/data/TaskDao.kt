package com.lifeos.app.feature.task.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class TaskDayCount(val day: Long, val count: Int)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :epochDay ORDER BY createdAt ASC")
    fun observeForDate(epochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startEpochDay AND :endEpochDay ORDER BY date ASC, createdAt ASC")
    fun observeInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Upsert
    suspend fun upsertTask(entity: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query(
        "SELECT date as day, COUNT(*) as count FROM tasks " +
            "WHERE completed = 1 AND date BETWEEN :startEpochDay AND :endEpochDay GROUP BY date",
    )
    suspend fun completionCountByDay(startEpochDay: Long, endEpochDay: Long): List<TaskDayCount>

    @Query(
        "SELECT date as day, COUNT(*) as count FROM tasks " +
            "WHERE date BETWEEN :startEpochDay AND :endEpochDay GROUP BY date",
    )
    suspend fun totalCountByDay(startEpochDay: Long, endEpochDay: Long): List<TaskDayCount>

    @Query("DELETE FROM tasks WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)
}
