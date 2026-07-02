package com.lifeos.app.feature.settings.domain

import kotlinx.coroutines.flow.Flow

interface NotificationPrefsRepository {
    fun observeSoundProfile(): Flow<SoundProfile>
    suspend fun setSoundProfile(profile: SoundProfile)
}
