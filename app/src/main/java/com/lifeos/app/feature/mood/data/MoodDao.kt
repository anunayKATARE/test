package com.lifeos.app.feature.mood.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class TriggerCount(val trigger: String, val count: Int)
data class EmotionCount(val emotion: String, val count: Int)

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries ORDER BY dateTime DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE dateTime BETWEEN :startMillis AND :endMillis ORDER BY dateTime DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: String): MoodEntryEntity?

    @Upsert
    suspend fun upsert(entity: MoodEntryEntity)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "SELECT trigger, COUNT(*) as count FROM mood_entries WHERE trigger != '' " +
            "GROUP BY trigger ORDER BY count DESC LIMIT :limit",
    )
    suspend fun mostCommonTriggers(limit: Int): List<TriggerCount>

    @Query(
        "SELECT emotion, COUNT(*) as count FROM mood_entries " +
            "WHERE dateTime BETWEEN :startMillis AND :endMillis GROUP BY emotion ORDER BY count DESC",
    )
    suspend fun emotionFrequency(startMillis: Long, endMillis: Long): List<EmotionCount>

    @Query("DELETE FROM mood_entries WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)
}
