package com.lifeos.app.feature.inspiration.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InspirationDao {
    @Query("SELECT * FROM inspiration_items ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<InspirationEntity>>

    @Upsert
    suspend fun upsert(entity: InspirationEntity)

    @Query("DELETE FROM inspiration_items WHERE id = :id")
    suspend fun delete(id: String)
}
