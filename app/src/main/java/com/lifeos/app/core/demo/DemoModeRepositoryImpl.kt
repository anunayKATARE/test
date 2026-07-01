package com.lifeos.app.core.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lifeos.app.feature.profile.data.ProfileDao
import com.lifeos.app.feature.profile.data.toDomain
import com.lifeos.app.feature.profile.data.toEntity
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DemoModeRepositoryImpl @Inject constructor(
    private val dao: ProfileDao,
    private val dataStore: DataStore<Preferences>,
) : DemoModeRepository {

    private val activeProfileIdFlow: Flow<String?> =
        dataStore.data.map { prefs -> prefs[ACTIVE_PROFILE_KEY] }

    override val activeProfile: Flow<Profile?> =
        combine(dao.observeAll(), activeProfileIdFlow) { profiles, activeId ->
            if (activeId == null) return@combine null
            profiles.find { it.id == activeId }?.toDomain()
                ?: DemoTemplate.fromId(activeId)?.let { template ->
                    // Legacy: activated before the profiles table existed; synthesise from known template
                    Profile(id = template.id, name = template.label, isDemo = true, demoTemplate = template, createdAt = Instant.EPOCH)
                }
        }

    override val allProfiles: Flow<List<Profile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun setActiveProfile(profile: Profile?) {
        dataStore.edit { prefs ->
            if (profile == null) prefs.remove(ACTIVE_PROFILE_KEY)
            else prefs[ACTIVE_PROFILE_KEY] = profile.id
        }
    }

    override suspend fun upsertProfile(profile: Profile) {
        dao.upsert(profile.toEntity())
    }

    override suspend fun createUserProfile(name: String): Profile {
        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = name,
            isDemo = false,
            demoTemplate = null,
            createdAt = Instant.now(),
        )
        dao.upsert(profile.toEntity())
        return profile
    }

    override suspend fun deleteProfile(id: String) {
        dao.deleteById(id)
    }

    companion object {
        private val ACTIVE_PROFILE_KEY = stringPreferencesKey("active_demo_profile_id")
    }
}
