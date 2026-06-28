package com.lifeos.app.feature.mentaltoughness.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import kotlinx.coroutines.flow.Flow

data class MentalToughnessTypeCount(val type: MentalToughnessType, val count: Int)

@Dao
interface MentalToughnessDao {
    @Query("SELECT * FROM mental_toughness_entries ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<MentalToughnessEntity>>

    @Query("SELECT * FROM mental_toughness_entries WHERE type = :type ORDER BY dateTime DESC")
    fun observeByType(type: MentalToughnessType): Flow<List<MentalToughnessEntity>>

    @Query("SELECT * FROM mental_toughness_entries WHERE id = :id")
    suspend fun getById(id: String): MentalToughnessEntity?

    @Upsert
    suspend fun upsert(entity: MentalToughnessEntity)

    @Query("DELETE FROM mental_toughness_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT type, COUNT(*) as count FROM mental_toughness_entries GROUP BY type")
    suspend fun countByType(): List<MentalToughnessTypeCount>
}
