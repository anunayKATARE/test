package com.lifeos.app.feature.demo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.demo.DemoModeManager
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.core.demo.DemoProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DemoModeViewModel @Inject constructor(
    demoModeRepository: DemoModeRepository,
    private val demoModeManager: DemoModeManager,
) : ViewModel() {

    val activeProfile: StateFlow<DemoProfile?> = demoModeRepository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val profiles: List<DemoProfile> = DemoProfile.entries

    fun activate(profile: DemoProfile) {
        viewModelScope.launch { demoModeManager.activate(profile) }
    }

    fun deactivate() {
        viewModelScope.launch { demoModeManager.deactivate() }
    }
}
