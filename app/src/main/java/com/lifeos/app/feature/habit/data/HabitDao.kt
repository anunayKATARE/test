package com.lifeos.app.feature.habit.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class HabitDayCount(val day: Long, val count: Int)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Upsert
    suspend fun upsertHabit(entity: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :id")
    suspend fun deleteCompletionsForHabit(id: String)

    @Query("UPDATE habits SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun observeCompletionsForHabit(habitId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date = :epochDay")
    fun observeCompletionsOn(epochDay: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getCompletionsBetween(habitId: String, startEpochDay: Long, endEpochDay: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :epochDay LIMIT 1")
    suspend fun getCompletion(habitId: String, epochDay: Long): HabitCompletionEntity?

    @Upsert
    suspend fun upsertCompletion(entity: HabitCompletionEntity)

    @Query(
        "SELECT date as day, COUNT(*) as count FROM habit_completions " +
            "WHERE completed = 1 AND date BETWEEN :startEpochDay AND :endEpochDay GROUP BY date",
    )
    suspend fun completionCountByDay(startEpochDay: Long, endEpochDay: Long): List<HabitDayCount>

    @Query("DELETE FROM habits WHERE profileId = :profileId")
    suspend fun deleteHabitsByProfile(profileId: String)

    @Query("DELETE FROM habit_completions WHERE profileId = :profileId")
    suspend fun deleteCompletionsByProfile(profileId: String)

    @Query("SELECT * FROM habits WHERE profileId IS NULL")
    suspend fun getAllRealHabits(): List<HabitEntity>

    @Query("DELETE FROM habits WHERE profileId IS NULL")
    suspend fun deleteAllRealHabits()

    @Query("SELECT * FROM habit_completions WHERE profileId IS NULL")
    suspend fun getAllRealCompletions(): List<HabitCompletionEntity>

    @Query("DELETE FROM habit_completions WHERE profileId IS NULL")
    suspend fun deleteAllRealCompletions()
}
