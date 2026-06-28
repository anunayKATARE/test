package com.lifeos.app.feature.problemsolver.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE status != 'RESOLVED' ORDER BY updatedAt DESC")
    fun observeOpen(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun getById(id: String): ProblemEntity?

    @Upsert
    suspend fun upsert(entity: ProblemEntity)

    @Query("DELETE FROM problems WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE problems SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: ProblemStatus)
}
