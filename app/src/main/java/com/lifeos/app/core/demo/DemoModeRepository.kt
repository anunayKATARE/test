package com.lifeos.app.core.demo

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for which [Profile] is currently active, if any. A null active profile
 * means the app is showing the user's real data. User-created profiles are persisted in Room;
 * the active profile ID is stored in DataStore for fast cold-start access.
 */
interface DemoModeRepository {
    val activeProfile: Flow<Profile?>
    val allProfiles: Flow<List<Profile>>
    suspend fun setActiveProfile(profile: Profile?)
    suspend fun upsertProfile(profile: Profile)
    suspend fun createUserProfile(name: String): Profile
    suspend fun deleteProfile(id: String)
}
