package com.lifeos.app.core.demo

import kotlinx.coroutines.flow.Flow

interface DemoModeRepository {
    val activeProfile: Flow<Profile?>
    val allProfiles: Flow<List<Profile>>
    suspend fun setActiveProfile(profile: Profile?)
    suspend fun upsertProfile(profile: Profile)
    suspend fun createUserProfile(name: String): Profile
    suspend fun deleteProfile(id: String)
    suspend fun renameProfile(id: String, name: String)

    companion object {
        const val DEFAULT_PROFILE_ID = "__default__"
    }
}
