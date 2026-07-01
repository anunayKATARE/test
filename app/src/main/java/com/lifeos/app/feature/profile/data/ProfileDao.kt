package com.lifeos.app.feature.profile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Upsert
    suspend fun upsert(entity: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProfileEntity?
}
