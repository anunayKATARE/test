package com.lifeos.app.feature.calendar.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.habit.domain.CompleteHabitUseCase
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.habit.domain.UncompleteHabitUseCase
import com.lifeos.app.feature.task.domain.AddTaskUseCase
import com.lifeos.app.feature.task.domain.TaskRepository
import com.lifeos.app.feature.task.domain.ToggleTaskCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DayItemUiModel(val id: String, val title: String, val completed: Boolean, val isHabit: Boolean)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val uncompleteHabitUseCase: UncompleteHabitUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
) : ViewModel() {

    val date: LocalDate = LocalDate.parse(checkNotNull(savedStateHandle.get<String>("date")))

    private val _items = MutableStateFlow<List<DayItemUiModel>>(emptyList())
    val items: StateFlow<List<DayItemUiModel>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                habitRepository.observeActiveHabits(),
                habitRepository.observeCompletionsOn(date),
                taskRepository.observeTasksForDate(date),
            ) { habits, completions, tasks ->
                val completedHabitIds = completions.filter { it.completed }.map { it.habitId }.toSet()
                val habitItems = habits.filter { isHabitScheduledOn(it, date) }
                    .map { DayItemUiModel(it.id, it.title, completedHabitIds.contains(it.id), isHabit = true) }
                val taskItems = tasks.map { DayItemUiModel(it.id, it.title, it.completed, isHabit = false) }
                habitItems + taskItems
            }.collect { _items.value = it }
        }
    }

    fun toggle(item: DayItemUiModel) {
        viewModelScope.launch {
            if (item.isHabit) {
                if (item.completed) uncompleteHabitUseCase(item.id, date) else completeHabitUseCase(item.id, date)
            } else {
                toggleTaskCompletionUseCase(item.id, !item.completed)
            }
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch { addTaskUseCase(title, date) }
    }

    private fun isHabitScheduledOn(habit: Habit, date: LocalDate): Boolean = when (habit.scheduleType) {
        HabitScheduleType.DAILY, HabitScheduleType.WEEKLY, HabitScheduleType.MONTHLY -> true
        HabitScheduleType.CUSTOM -> habit.customDaysOfWeek.contains(date.dayOfWeek.value)
    }
}
