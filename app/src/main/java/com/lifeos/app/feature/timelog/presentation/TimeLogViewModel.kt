package com.lifeos.app.feature.timelog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import com.lifeos.app.feature.timelog.domain.TimeLog
import com.lifeos.app.feature.timelog.domain.TimeLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class TimeLogFormState(
    val showStartSheet: Boolean = false,
    val entryChore: String = "",
    val entryTaskId: String? = null,
    val recentChores: List<String> = emptyList(),
)

data class TimeLogUiState(
    val todaysLogs: List<TimeLog> = emptyList(),
    val activeTimer: TimeLog? = null,
    val todaysTasks: List<Task> = emptyList(),
    val recentChores: List<String> = emptyList(),
    val showStartSheet: Boolean = false,
    val entryChore: String = "",
    val entryTaskId: String? = null,
    val loggingEnabled: Boolean = true,
)

@HiltViewModel
class TimeLogViewModel @Inject constructor(
    private val timeLogRepository: TimeLogRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val today = LocalDate.now()
    private val _form = MutableStateFlow(TimeLogFormState())

    val uiState: StateFlow<TimeLogUiState> = combine(
        timeLogRepository.observeLogsForDay(today),
        timeLogRepository.observeActiveTimer(),
        taskRepository.observeTasksForDate(today),
        timeLogRepository.observeLoggingEnabled(),
        _form,
    ) { logs, timer, tasks, enabled, form ->
        TimeLogUiState(
            todaysLogs = logs,
            activeTimer = timer,
            todaysTasks = tasks,
            recentChores = form.recentChores,
            showStartSheet = form.showStartSheet,
            entryChore = form.entryChore,
            entryTaskId = form.entryTaskId,
            loggingEnabled = enabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeLogUiState())

    init {
        viewModelScope.launch {
            val chores = timeLogRepository.getRecentChores()
            _form.update { it.copy(recentChores = chores) }
        }
    }

    fun openStartSheet() { _form.update { it.copy(showStartSheet = true, entryChore = "", entryTaskId = null) } }
    fun dismissStartSheet() { _form.update { it.copy(showStartSheet = false) } }
    fun updateEntryChore(v: String) { _form.update { it.copy(entryChore = v) } }
    fun selectEntryTask(taskId: String?) { _form.update { it.copy(entryTaskId = taskId) } }

    fun startTimer() {
        val form = _form.value
        val chore = form.entryChore.trim().ifBlank { null }
        val taskId = form.entryTaskId
        if (chore == null && taskId == null) return
        viewModelScope.launch {
            timeLogRepository.startTimer(linkedTaskId = taskId, chore = chore)
            val chores = timeLogRepository.getRecentChores()
            _form.update { it.copy(showStartSheet = false, recentChores = chores) }
        }
    }

    fun stopTimer() {
        val timerId = uiState.value.activeTimer?.id ?: return
        viewModelScope.launch { timeLogRepository.stopTimer(timerId) }
    }

    fun logManual() {
        val form = _form.value
        val chore = form.entryChore.trim().ifBlank { null }
        val taskId = form.entryTaskId
        if (chore == null && taskId == null) return
        viewModelScope.launch {
            val now = Instant.now()
            val log = TimeLog(
                id = UUID.randomUUID().toString(),
                startedAt = now.minusSeconds(1800),
                endedAt = now,
                linkedTaskId = taskId,
                chore = chore,
            )
            timeLogRepository.saveLog(log)
            val chores = timeLogRepository.getRecentChores()
            _form.update { it.copy(showStartSheet = false, recentChores = chores) }
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch { timeLogRepository.deleteLog(logId) }
    }

    fun toggleLoggingEnabled() {
        viewModelScope.launch {
            timeLogRepository.setLoggingEnabled(!uiState.value.loggingEnabled)
        }
    }
}
