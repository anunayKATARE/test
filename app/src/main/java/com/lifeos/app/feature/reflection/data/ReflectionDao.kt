package com.lifeos.app.feature.reflection.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflection_entries ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflection_entries WHERE templateType = :type ORDER BY dateTime DESC")
    fun observeByType(type: ReflectionTemplateType): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflection_entries WHERE id = :id")
    suspend fun getById(id: String): ReflectionEntity?

    @Upsert
    suspend fun upsert(entity: ReflectionEntity)

    @Query("DELETE FROM reflection_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reflection_entries WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("SELECT * FROM reflection_entries WHERE profileId IS NULL")
    suspend fun getAllReal(): List<ReflectionEntity>

    @Query("DELETE FROM reflection_entries WHERE profileId IS NULL")
    suspend fun deleteAllReal()
}
