package com.lifeos.app.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.isScheduledOn
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    val intensityByDate: StateFlow<Map<LocalDate, Float>> = _month.flatMapLatest { month ->
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        combine(
            habitRepository.observeActiveHabits(),
            habitRepository.observeCompletionsByDay(start, end),
            taskRepository.observeCompletionsByDay(start, end),
            taskRepository.observeTotalByDay(start, end),
        ) { habits, habitCompletions, taskCompletions, taskTotals ->
            buildIntensityMap(start, end, habits, habitCompletions, taskCompletions, taskTotals)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun previousMonth() { _month.value = _month.value.minusMonths(1) }

    private fun buildIntensityMap(
        start: LocalDate,
        end: LocalDate,
        habits: List<Habit>,
        habitCompletions: Map<Long, Int>,
        taskCompletions: Map<Long, Int>,
        taskTotals: Map<Long, Int>,
    ): Map<LocalDate, Float> {
        val result = mutableMapOf<LocalDate, Float>()
        var date = start
        while (!date.isAfter(end)) {
            val scheduledHabits = habits.count { it.isScheduledOn(date) }
            val completedHabits = (habitCompletions[date.toEpochDay()] ?: 0).coerceAtMost(scheduledHabits)
            val totalTasks = taskTotals[date.toEpochDay()] ?: 0
            val completedTasks = taskCompletions[date.toEpochDay()] ?: 0
            val total = scheduledHabits + totalTasks
            if (total > 0) {
                result[date] = ((completedHabits + completedTasks).toFloat() / total).coerceIn(0f, 1f)
            }
            date = date.plusDays(1)
        }
        return result
    }
}
