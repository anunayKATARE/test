package com.lifeos.app.feature.task.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.calendar.domain.AvailabilityService
import com.lifeos.app.feature.calendar.domain.CalendarEvent
import com.lifeos.app.feature.calendar.domain.CalendarRepository
import com.lifeos.app.feature.calendar.domain.FreeSlot
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskFormState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val triggers: List<String> = emptyList(),
    val triggerInput: String = "",
    val scheduledAt: Instant? = null,
)

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val showSheet: Boolean = false,
    val editingTask: Task? = null,
    val form: TaskFormState = TaskFormState(),
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val freeSlots: List<FreeSlot> = emptyList(),
    val calendarPermissionGranted: Boolean = false,
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val availabilityService: AvailabilityService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState(),
    )

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val tasks: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { taskRepository.observeTasksForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            tasks.collect { list ->
                _uiState.update { it.copy(tasks = list) }
            }
        }
        viewModelScope.launch {
            _selectedDate.collect { date ->
                _uiState.update { it.copy(selectedDate = date) }
                refreshCalendar(date)
            }
        }
        _uiState.update { it.copy(calendarPermissionGranted = calendarRepository.hasPermission()) }
    }

    fun onCalendarPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(calendarPermissionGranted = granted) }
        if (granted) refreshCalendar(_selectedDate.value)
    }

    private fun refreshCalendar(date: LocalDate) {
        viewModelScope.launch {
            val events = calendarRepository.getEventsForDay(date)
            val slots = availabilityService.findFreeSlots(events, date)
            _uiState.update { it.copy(calendarEvents = events, freeSlots = slots) }
        }
    }

    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun openAddSheet() {
        _uiState.update {
            it.copy(showSheet = true, editingTask = null, form = TaskFormState(date = _selectedDate.value))
        }
    }

    fun openEditSheet(task: Task) {
        _uiState.update {
            it.copy(
                showSheet = true,
                editingTask = task,
                form = TaskFormState(
                    title = task.title,
                    description = task.description,
                    date = task.date,
                    triggers = task.triggers,
                    scheduledAt = task.scheduledAt,
                ),
            )
        }
    }

    fun dismissSheet() { _uiState.update { it.copy(showSheet = false) } }

    fun updateTitle(v: String) { _uiState.update { it.copy(form = it.form.copy(title = v)) } }
    fun updateDescription(v: String) { _uiState.update { it.copy(form = it.form.copy(description = v)) } }
    fun updateDate(v: LocalDate) { _uiState.update { it.copy(form = it.form.copy(date = v)) } }
    fun updateTriggerInput(v: String) { _uiState.update { it.copy(form = it.form.copy(triggerInput = v)) } }
    fun pickScheduledSlot(slot: FreeSlot?) {
        _uiState.update { it.copy(form = it.form.copy(scheduledAt = slot?.start)) }
    }

    fun addTrigger() {
        val input = _uiState.value.form.triggerInput.trim().lowercase()
        if (input.isBlank()) return
        _uiState.update { state ->
            val newTriggers = (state.form.triggers + input).distinct()
            state.copy(form = state.form.copy(triggers = newTriggers, triggerInput = ""))
        }
    }

    fun removeTrigger(trigger: String) {
        _uiState.update { it.copy(form = it.form.copy(triggers = it.form.triggers - trigger)) }
    }

    fun saveTask() {
        val form = _uiState.value.form
        if (form.title.isBlank()) return
        val existing = _uiState.value.editingTask
        val task = Task(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = form.title.trim(),
            description = form.description.trim(),
            date = form.date,
            completed = existing?.completed ?: false,
            createdAt = existing?.createdAt ?: Instant.now(),
            triggers = form.triggers,
            scheduledAt = form.scheduledAt,
        )
        viewModelScope.launch { taskRepository.upsertTask(task) }
        _uiState.update { it.copy(showSheet = false) }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { taskRepository.setCompleted(task.id, !task.completed) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task.id) }
    }
}
