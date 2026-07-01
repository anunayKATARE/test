package com.lifeos.app.feature.timelog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.timelog.domain.TimeLog
import com.lifeos.app.feature.timelog.domain.TimeLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val THIRTY_MIN_MILLIS = 30 * 60_000L
private const val TEN_MIN_MILLIS = 10 * 60_000L

@HiltViewModel
class TimeLogPromptViewModel @Inject constructor(
    private val timeLogRepository: TimeLogRepository,
) : ViewModel() {

    private val _showPrompt = MutableStateFlow(false)
    val showPrompt: StateFlow<Boolean> = _showPrompt.asStateFlow()

    init {
        viewModelScope.launch { checkShouldPrompt() }
    }

    private suspend fun checkShouldPrompt() {
        val activeTimer = timeLogRepository.observeActiveTimer().first()
        if (activeTimer != null) return
        val lastEntry = timeLogRepository.getLastEntryMillis()
        if (lastEntry == 0L) {
            // Fresh install — wait 10 minutes before the first nudge
            delay(TEN_MIN_MILLIS)
            if (timeLogRepository.observeActiveTimer().first() == null) {
                _showPrompt.value = true
            }
            return
        }
        if (System.currentTimeMillis() - lastEntry >= THIRTY_MIN_MILLIS) {
            _showPrompt.value = true
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            timeLogRepository.touchLastEntryMillis()
            _showPrompt.value = false
        }
    }

    fun logChore(chore: String) {
        if (chore.isBlank()) return
        viewModelScope.launch {
            val log = TimeLog(
                id = UUID.randomUUID().toString(),
                startedAt = Instant.now().minusSeconds(1800),
                endedAt = Instant.now(),
                chore = chore.trim(),
            )
            timeLogRepository.saveLog(log)
            _showPrompt.value = false
        }
    }
}
