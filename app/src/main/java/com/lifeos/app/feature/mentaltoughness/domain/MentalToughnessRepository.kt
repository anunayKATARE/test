package com.lifeos.app.feature.mentaltoughness.domain

import kotlinx.coroutines.flow.Flow

interface MentalToughnessRepository {
    fun observeAll(): Flow<List<MentalToughnessEntry>>
    fun observeByType(type: MentalToughnessType): Flow<List<MentalToughnessEntry>>
    suspend fun getById(id: String): MentalToughnessEntry?
    suspend fun upsert(entry: MentalToughnessEntry)
    suspend fun delete(id: String)
    suspend fun countByType(): Map<MentalToughnessType, Int>
}
