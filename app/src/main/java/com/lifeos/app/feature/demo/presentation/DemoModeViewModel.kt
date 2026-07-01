package com.lifeos.app.feature.demo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.demo.DemoModeManager
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.core.demo.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfilesUiState(
    val activeProfile: Profile? = null,
    val userProfiles: List<Profile> = emptyList(),
    val showCreateDialog: Boolean = false,
    val renamingProfile: Profile? = null,
)

@HiltViewModel
class DemoModeViewModel @Inject constructor(
    private val demoModeRepository: DemoModeRepository,
    private val demoModeManager: DemoModeManager,
) : ViewModel() {

    val demoTemplates: List<DemoTemplate> = DemoTemplate.entries
    val defaultProfileId: String = DemoModeRepository.DEFAULT_PROFILE_ID

    private val _showCreateDialog = MutableStateFlow(false)
    private val _renamingProfile = MutableStateFlow<Profile?>(null)

    val uiState: StateFlow<ProfilesUiState> = combine(
        demoModeRepository.activeProfile,
        demoModeRepository.allProfiles,
        _showCreateDialog,
        _renamingProfile,
    ) { active, profiles, showCreate, renamingProfile ->
        ProfilesUiState(
            activeProfile = active,
            userProfiles = profiles.filter { !it.isDemo },
            showCreateDialog = showCreate,
            renamingProfile = renamingProfile,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilesUiState())

    fun activateDemoTemplate(template: DemoTemplate) {
        viewModelScope.launch { demoModeManager.activateDemoTemplate(template) }
    }

    fun activateProfile(profile: Profile) {
        viewModelScope.launch { demoModeManager.activateProfile(profile) }
    }

    fun createUserProfile(name: String) {
        viewModelScope.launch { demoModeManager.createUserProfile(name) }
        _showCreateDialog.value = false
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch { demoModeManager.deleteProfile(profileId) }
    }

    fun renameProfile(id: String, name: String) {
        viewModelScope.launch { demoModeManager.renameProfile(id, name) }
        _renamingProfile.value = null
    }

    fun deactivate() {
        viewModelScope.launch { demoModeManager.deactivate() }
    }

    fun openCreateDialog() { _showCreateDialog.value = true }
    fun dismissCreateDialog() { _showCreateDialog.value = false }
    fun startRename(profile: Profile) { _renamingProfile.value = profile }
    fun dismissRenameDialog() { _renamingProfile.value = null }
}
