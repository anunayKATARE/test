package com.lifeos.app.feature.goal.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE horizon = :horizon AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun observeByHorizon(horizon: GoalHorizon): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(entity: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE goals SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: GoalStatus)

    @Query("DELETE FROM goals WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM goals WHERE profileId IS NULL")
    suspend fun getAllReal(): List<GoalEntity>

    @Query("DELETE FROM goals WHERE profileId IS NULL")
    suspend fun deleteAllReal()
}
