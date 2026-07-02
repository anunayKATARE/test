package com.lifeos.app.feature.task.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.calendar.domain.AvailabilityService
import com.lifeos.app.feature.calendar.domain.CalendarEvent
import com.lifeos.app.feature.calendar.domain.CalendarRepository
import com.lifeos.app.feature.calendar.domain.FreeSlot
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.isScheduledOn
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitDayItem(
    val id: String,
    val title: String,
    val triggers: List<String>,
    val completedToday: Boolean,
)

data class TaskFormState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val triggers: List<String> = emptyList(),
    val triggerInput: String = "",
    val scheduledAt: Instant? = null,
    val isChore: Boolean = false,
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
    val scheduledHabits: List<HabitDayItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val availabilityService: AvailabilityService,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // Separate permission Flow so granting permission re-triggers calendar observation
    private val _permissionGranted = MutableStateFlow(calendarRepository.hasPermission())

    private val _formAndSheet = MutableStateFlow(
        TaskUiState(calendarPermissionGranted = _permissionGranted.value),
    )

    private val _habitItems: StateFlow<List<HabitDayItem>> = combine(
        _selectedDate,
        habitRepository.observeActiveHabits(),
    ) { date, habits ->
        habits.filter { it.isScheduledOn(date) } to date
    }.flatMapLatest { (filtered, date) ->
        if (filtered.isEmpty()) flowOf(emptyList())
        else habitRepository.observeCompletionsOn(date).map { completions ->
            val completedIds = completions.filter { it.completed }.map { it.habitId }.toSet()
            filtered.map { h -> HabitDayItem(h.id, h.title, h.triggers, h.id in completedIds) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<TaskUiState> = combine(
        combine(
            _selectedDate.flatMapLatest { taskRepository.observeTasksForDate(it) },
            _selectedDate,
            // When either date or permission changes, re-subscribe to the calendar Flow
            combine(_selectedDate, _permissionGranted) { date, granted -> date to granted }
                .flatMapLatest { (date, granted) ->
                    if (granted) calendarRepository.observeEventsForDay(date)
                    else flowOf(emptyList())
                },
            _permissionGranted,
            _formAndSheet,
        ) { tasks, date, events, granted, formState ->
            val slots = availabilityService.findFreeSlots(events, date)
            formState.copy(
                tasks = tasks,
                selectedDate = date,
                calendarEvents = events,
                freeSlots = slots,
                calendarPermissionGranted = granted,
            )
        },
        _habitItems,
    ) { state, habits -> state.copy(scheduledHabits = habits) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

    fun onCalendarPermissionResult(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun openAddSheet() {
        _formAndSheet.update {
            it.copy(showSheet = true, editingTask = null, form = TaskFormState(date = _selectedDate.value))
        }
    }

    fun openEditSheet(task: Task) {
        _formAndSheet.update {
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

    fun dismissSheet() { _formAndSheet.update { it.copy(showSheet = false) } }

    fun updateTitle(v: String) { _formAndSheet.update { it.copy(form = it.form.copy(title = v)) } }
    fun updateDescription(v: String) { _formAndSheet.update { it.copy(form = it.form.copy(description = v)) } }
    fun updateDate(v: LocalDate) { _formAndSheet.update { it.copy(form = it.form.copy(date = v)) } }
    fun updateTriggerInput(v: String) { _formAndSheet.update { it.copy(form = it.form.copy(triggerInput = v)) } }
    fun pickScheduledSlot(slot: FreeSlot?) {
        _formAndSheet.update { it.copy(form = it.form.copy(scheduledAt = slot?.start)) }
    }

    fun pickScheduledTime(instant: java.time.Instant) {
        _formAndSheet.update { it.copy(form = it.form.copy(scheduledAt = instant)) }
    }

    fun updateIsChore(v: Boolean) { _formAndSheet.update { it.copy(form = it.form.copy(isChore = v)) } }

    fun addTrigger() {
        val input = _formAndSheet.value.form.triggerInput.trim().lowercase()
        if (input.isBlank()) return
        _formAndSheet.update { state ->
            val newTriggers = (state.form.triggers + input).distinct()
            state.copy(form = state.form.copy(triggers = newTriggers, triggerInput = ""))
        }
    }

    fun removeTrigger(trigger: String) {
        _formAndSheet.update { it.copy(form = it.form.copy(triggers = it.form.triggers - trigger)) }
    }

    fun saveTask() {
        val form = _formAndSheet.value.form
        if (form.title.isBlank()) return
        val existing = _formAndSheet.value.editingTask
        val task = Task(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = form.title.trim(),
            description = form.description.trim(),
            date = form.date,
            completed = existing?.completed ?: false,
            createdAt = existing?.createdAt ?: Instant.now(),
            triggers = form.triggers,
            scheduledAt = form.scheduledAt,
            isChore = form.isChore,
        )
        viewModelScope.launch { taskRepository.upsertTask(task) }
        _formAndSheet.update { it.copy(showSheet = false) }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { taskRepository.setCompleted(task.id, !task.completed) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task.id) }
    }

    fun toggleHabitCompleted(item: HabitDayItem) {
        viewModelScope.launch {
            habitRepository.setCompletion(item.id, _selectedDate.value, !item.completedToday)
        }
    }
}
