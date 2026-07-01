package com.lifeos.app.feature.checkin.domain

import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    fun observeActive(): Flow<CheckInSession?>
    suspend fun upsert(session: CheckInSession)
    suspend fun complete(id: String)
}
