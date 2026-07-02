package com.lifeos.app.feature.timelog.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeLogDao {
    @Query(
        "SELECT * FROM time_logs WHERE startedAt >= :startEpochMillis AND startedAt < :endEpochMillis " +
            "ORDER BY startedAt DESC",
    )
    fun observeForDay(startEpochMillis: Long, endEpochMillis: Long): Flow<List<TimeLogEntity>>

    @Query("SELECT * FROM time_logs WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveTimer(): Flow<TimeLogEntity?>

    @Query(
        "SELECT DISTINCT chore FROM time_logs WHERE chore IS NOT NULL AND profileId = :profileId " +
            "ORDER BY startedAt DESC LIMIT 20",
    )
    suspend fun getRecentChores(profileId: String): List<String?>

    @Upsert
    suspend fun upsert(entity: TimeLogEntity)

    @Query("UPDATE time_logs SET endedAt = :endedAtMillis WHERE id = :id")
    suspend fun stopTimer(id: String, endedAtMillis: Long)

    @Query("DELETE FROM time_logs WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM time_logs WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM time_logs")
    suspend fun getAllForBackup(): List<TimeLogEntity>

    @Query("DELETE FROM time_logs")
    suspend fun deleteAllForRestore()
}
