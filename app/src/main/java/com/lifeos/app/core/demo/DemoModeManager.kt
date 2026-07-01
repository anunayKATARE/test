package com.lifeos.app.core.demo

interface DemoModeManager {
    suspend fun activateDemoTemplate(template: DemoTemplate): Profile
    suspend fun activateProfile(profile: Profile)
    suspend fun createUserProfile(name: String): Profile
    suspend fun deleteProfile(profileId: String)
    suspend fun renameProfile(id: String, name: String)
    suspend fun deactivate()
}
