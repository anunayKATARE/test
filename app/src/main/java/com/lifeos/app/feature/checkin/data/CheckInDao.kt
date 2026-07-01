package com.lifeos.app.feature.checkin.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Transaction
    @Query("SELECT * FROM check_in_sessions WHERE completedAt IS NULL ORDER BY scheduledAt DESC LIMIT 1")
    fun observeActive(): Flow<CheckInSessionWithCommitments?>

    @Upsert
    suspend fun upsertSession(entity: CheckInSessionEntity)

    @Upsert
    suspend fun upsertCommitments(entities: List<CheckInCommitmentEntity>)

    @Query("DELETE FROM check_in_commitments WHERE sessionId = :sessionId")
    suspend fun deleteCommitmentsForSession(sessionId: String)

    @Query("UPDATE check_in_sessions SET completedAt = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: String, completedAt: Long)

    @Query("UPDATE check_in_commitments SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCommitmentCompleted(id: String, isCompleted: Boolean)

    @Query("DELETE FROM check_in_sessions WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)
}
