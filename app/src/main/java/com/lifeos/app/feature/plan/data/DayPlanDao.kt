package com.lifeos.app.feature.plan.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanDao {
    @Query("SELECT * FROM day_plans WHERE forDate = :epochDay ORDER BY plannedAt DESC LIMIT 1")
    fun observeForDate(epochDay: Long): Flow<DayPlanEntity?>

    @Upsert
    suspend fun upsert(entity: DayPlanEntity)

    @Query("DELETE FROM day_plans WHERE profileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)
}
