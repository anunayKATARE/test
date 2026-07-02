package com.lifeos.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import com.lifeos.app.feature.settings.domain.SoundProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationPrefsRepository: NotificationPrefsRepository,
) : ViewModel() {

    val soundProfile: StateFlow<SoundProfile> = notificationPrefsRepository
        .observeSoundProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundProfile.SOUND_AND_VIBRATE)

    fun setSoundProfile(profile: SoundProfile) {
        viewModelScope.launch { notificationPrefsRepository.setSoundProfile(profile) }
    }
}
