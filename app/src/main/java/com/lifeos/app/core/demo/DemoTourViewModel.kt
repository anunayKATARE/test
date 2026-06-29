package com.lifeos.app.core.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DemoTourViewModel @Inject constructor(
    private val tourState: DemoTourState,
) : ViewModel() {

    val progress: StateFlow<DemoTourProgress?> = tourState.currentStepIndex
        .map { index -> index?.let { DemoTourProgress(DemoTourSteps.ALL[it], it + 1, DemoTourSteps.ALL.size) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun skipStep() = tourState.skipStep()

    fun skipTour() = tourState.skipTour()
}
