package com.lifeos.app.core.demo

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks progress through the guided tour. Lives only in memory: the tour is a one-time,
 * in-session onboarding aid, not a persisted setting like the active [DemoProfile].
 */
@Singleton
class DemoTourState @Inject constructor() {

    private val _currentStepIndex = MutableStateFlow<Int?>(null)
    val currentStepIndex: StateFlow<Int?> = _currentStepIndex.asStateFlow()

    fun start() {
        _currentStepIndex.value = 0
    }

    fun skipStep() {
        val nextIndex = (_currentStepIndex.value ?: return) + 1
        _currentStepIndex.value = nextIndex.takeIf { it < DemoTourSteps.ALL.size }
    }

    fun skipTour() {
        _currentStepIndex.value = null
    }
}
