package com.lifeos.app.feature.selfbelief.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SelfBeliefDao {
    @Query("SELECT * FROM self_belief_reflections ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<SelfBeliefEntity>>

    @Query("SELECT * FROM self_belief_reflections WHERE id = :id")
    suspend fun getById(id: String): SelfBeliefEntity?

    @Upsert
    suspend fun upsert(entity: SelfBeliefEntity)

    @Query("DELETE FROM self_belief_reflections WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT strengthsThatRemain FROM self_belief_reflections WHERE strengthsThatRemain != ''")
    suspend fun allStrengths(): List<String>

    @Query("DELETE FROM self_belief_reflections WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM self_belief_reflections WHERE profileId IS NULL")
    suspend fun getAllReal(): List<SelfBeliefEntity>

    @Query("DELETE FROM self_belief_reflections WHERE profileId IS NULL")
    suspend fun deleteAllReal()

    @Query("SELECT * FROM self_belief_reflections")
    suspend fun getAllForBackup(): List<SelfBeliefEntity>

    @Query("DELETE FROM self_belief_reflections")
    suspend fun deleteAllForRestore()
}
