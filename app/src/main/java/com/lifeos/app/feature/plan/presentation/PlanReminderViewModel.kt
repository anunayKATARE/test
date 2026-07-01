package com.lifeos.app.feature.plan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.plan.domain.DayPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlanReminderViewModel @Inject constructor(
    private val planRepository: DayPlanRepository,
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        viewModelScope.launch { checkShouldPrompt() }
    }

    private suspend fun checkShouldPrompt() {
        val hour = LocalTime.now(ZoneId.systemDefault()).hour
        if (hour < 21) return
        if (planRepository.isPlanCompletedToday()) return
        val snoozedUntil = planRepository.getSnoozedUntil()
        if (snoozedUntil > System.currentTimeMillis()) return
        _showDialog.value = true
    }

    fun dismiss() { _showDialog.value = false }

    fun snooze(minutes: Int) {
        viewModelScope.launch {
            planRepository.snoozeUntil(System.currentTimeMillis() + minutes * 60_000L)
            _showDialog.value = false
        }
    }
}
