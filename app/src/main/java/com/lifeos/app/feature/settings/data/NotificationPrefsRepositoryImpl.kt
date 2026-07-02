package com.lifeos.app.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import com.lifeos.app.feature.settings.domain.SoundProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationPrefsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : NotificationPrefsRepository {

    private val soundProfileKey = stringPreferencesKey("notification_sound_profile")

    override fun observeSoundProfile(): Flow<SoundProfile> =
        dataStore.data.map { prefs ->
            val name = prefs[soundProfileKey] ?: SoundProfile.SOUND_AND_VIBRATE.name
            SoundProfile.entries.firstOrNull { it.name == name } ?: SoundProfile.SOUND_AND_VIBRATE
        }

    override suspend fun setSoundProfile(profile: SoundProfile) {
        dataStore.edit { it[soundProfileKey] = profile.name }
    }
}
