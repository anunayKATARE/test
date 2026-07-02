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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

private val HHmm: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
    val scheduledEndAt: Instant? = null,
    val startTimeText: String = "",
    val endTimeText: String = "",
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
    val showQuickPlan: Boolean = false,
    val unscheduledUpcoming: List<Task> = emptyList(),
    val quickPlanTimes: Map<String, String> = emptyMap(),
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

    private val _unscheduledUpcoming: StateFlow<List<Task>> = taskRepository
        .observeUnscheduledUpcoming(3)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<TaskUiState> = combine(
        combine(
            _selectedDate.flatMapLatest { taskRepository.observeTasksForDate(it) },
            _selectedDate,
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
        _unscheduledUpcoming,
    ) { state, habits, upcoming ->
        state.copy(scheduledHabits = habits, unscheduledUpcoming = upcoming)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

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
        val zone = ZoneId.systemDefault()
        val startText = task.scheduledAt?.let { HHmm.format(it.atZone(zone)) } ?: ""
        val endText = task.scheduledEndAt?.let { HHmm.format(it.atZone(zone)) } ?: ""
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
                    scheduledEndAt = task.scheduledEndAt,
                    startTimeText = startText,
                    endTimeText = endText,
                    isChore = task.isChore,
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
        val text = slot?.start?.let { HHmm.format(it.atZone(ZoneId.systemDefault())) } ?: ""
        _formAndSheet.update { it.copy(form = it.form.copy(scheduledAt = slot?.start, startTimeText = text)) }
    }

    fun pickScheduledTime(instant: Instant) {
        val text = HHmm.format(instant.atZone(ZoneId.systemDefault()))
        _formAndSheet.update { it.copy(form = it.form.copy(scheduledAt = instant, startTimeText = text)) }
    }

    fun updateIsChore(v: Boolean) { _formAndSheet.update { it.copy(form = it.form.copy(isChore = v)) } }

    fun selectHabitForTask(item: HabitDayItem) {
        _formAndSheet.update { it.copy(form = it.form.copy(title = item.title, isChore = false)) }
    }

    fun updateStartTimeText(v: String) {
        _formAndSheet.update { it.copy(form = it.form.copy(startTimeText = autoFormatTime(v))) }
    }

    fun updateEndTimeText(v: String) {
        _formAndSheet.update { it.copy(form = it.form.copy(endTimeText = autoFormatTime(v))) }
    }

    fun applyStartTimeText() {
        val form = _formAndSheet.value.form
        parseTime(form.startTimeText, form.date)?.let { instant ->
            _formAndSheet.update { it.copy(form = it.form.copy(scheduledAt = instant)) }
        }
    }

    fun applyEndTimeText() {
        val form = _formAndSheet.value.form
        parseTime(form.endTimeText, form.date)?.let { instant ->
            _formAndSheet.update { it.copy(form = it.form.copy(scheduledEndAt = instant)) }
        }
    }

    // Strips non-digits and auto-inserts a colon after the 2nd digit so users
    // only need to type digits — e.g. "1430" becomes "14:30".
    private fun autoFormatTime(input: String): String {
        val digits = input.filter { it.isDigit() }.take(4)
        return if (digits.length >= 3) "${digits.take(2)}:${digits.drop(2)}" else digits
    }

    // Parses "HH:mm", "HHmm" (4 digits), or "Hmm" (3 digits → 0H:mm).
    private fun parseTime(text: String, date: LocalDate): Instant? {
        val digits = text.filter { it.isDigit() }
        val normalized = when (digits.length) {
            3 -> "0${digits[0]}:${digits.drop(1)}"
            4 -> "${digits.take(2)}:${digits.drop(2)}"
            else -> text
        }
        return try {
            LocalTime.parse(normalized, HHmm).atDate(date).atZone(ZoneId.systemDefault()).toInstant()
        } catch (_: DateTimeParseException) { null }
    }

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
            scheduledEndAt = form.scheduledEndAt,
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

    fun openQuickPlan() {
        _formAndSheet.update { it.copy(showQuickPlan = true, quickPlanTimes = emptyMap()) }
    }

    fun dismissQuickPlan() {
        _formAndSheet.update { it.copy(showQuickPlan = false) }
    }

    fun updateQuickPlanTime(taskId: String, time: String) {
        _formAndSheet.update { it.copy(quickPlanTimes = it.quickPlanTimes + (taskId to autoFormatTime(time))) }
    }

    fun confirmQuickPlan() {
        val state = _formAndSheet.value
        val today = LocalDate.now()
        viewModelScope.launch {
            state.unscheduledUpcoming.forEach { task ->
                val timeText = state.quickPlanTimes[task.id]?.trim() ?: return@forEach
                if (timeText.isBlank()) return@forEach
                val taskDate = if (task.date >= today) task.date else today
                parseTime(timeText, taskDate)?.let { instant ->
                    taskRepository.upsertTask(task.copy(scheduledAt = instant))
                }
            }
            _formAndSheet.update { it.copy(showQuickPlan = false, quickPlanTimes = emptyMap()) }
        }
    }
}
