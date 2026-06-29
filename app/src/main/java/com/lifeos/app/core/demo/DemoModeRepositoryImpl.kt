package com.lifeos.app.core.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoModeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : DemoModeRepository {

    override val activeProfile: Flow<DemoProfile?> =
        dataStore.data.map { prefs -> DemoProfile.fromId(prefs[ACTIVE_PROFILE_KEY]) }

    override suspend fun setActiveProfile(profile: DemoProfile?) {
        dataStore.edit { prefs ->
            if (profile == null) prefs.remove(ACTIVE_PROFILE_KEY) else prefs[ACTIVE_PROFILE_KEY] = profile.id
        }
    }

    companion object {
        private val ACTIVE_PROFILE_KEY = stringPreferencesKey("active_demo_profile_id")
    }
}
