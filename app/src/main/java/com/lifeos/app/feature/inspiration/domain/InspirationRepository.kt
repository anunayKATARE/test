package com.lifeos.app.feature.inspiration.domain

import kotlinx.coroutines.flow.Flow

interface InspirationRepository {
    fun observeAll(): Flow<List<InspirationItem>>
    suspend fun upsert(item: InspirationItem)
    suspend fun delete(id: String)
}
