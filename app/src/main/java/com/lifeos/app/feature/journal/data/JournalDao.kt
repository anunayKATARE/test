package com.lifeos.app.feature.journal.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class DayCount(val day: Long, val count: Int)

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY dateTime DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dateTime BETWEEN :startMillis AND :endMillis ORDER BY dateTime DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: String): JournalEntryEntity?

    @Upsert
    suspend fun upsert(entity: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "SELECT * FROM journal_entries WHERE title LIKE '%' || :query || '%' " +
            "OR body LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' " +
            "ORDER BY dateTime DESC",
    )
    suspend fun search(query: String): List<JournalEntryEntity>

    @Query(
        "SELECT (dateTime / 86400000) as day, COUNT(*) as count FROM journal_entries " +
            "WHERE dateTime BETWEEN :startMillis AND :endMillis GROUP BY day",
    )
    suspend fun countByDay(startMillis: Long, endMillis: Long): List<DayCount>

    @Query("DELETE FROM journal_entries WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM journal_entries WHERE profileId IS NULL")
    suspend fun getAllReal(): List<JournalEntryEntity>

    @Query("DELETE FROM journal_entries WHERE profileId IS NULL")
    suspend fun deleteAllReal()

    @Query("SELECT * FROM journal_entries")
    suspend fun getAllForBackup(): List<JournalEntryEntity>

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllForRestore()
}
