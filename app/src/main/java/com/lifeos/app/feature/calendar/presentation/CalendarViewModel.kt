package com.lifeos.app.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _intensityByDate = MutableStateFlow<Map<LocalDate, Float>>(emptyMap())
    val intensityByDate: StateFlow<Map<LocalDate, Float>> = _intensityByDate.asStateFlow()

    init {
        load()
    }

    fun nextMonth() {
        _month.value = _month.value.plusMonths(1)
        load()
    }

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val month = _month.value
            val start = month.atDay(1)
            val end = month.atEndOfMonth()

            val habits = habitRepository.observeActiveHabits().first()
            val habitCompletedByDay = habitRepository.completionCountByDay(start, end)
            val taskTotalByDay = taskRepository.totalCountByDay(start, end)
            val taskCompletedByDay = taskRepository.completionCountByDay(start, end)

            val result = mutableMapOf<LocalDate, Float>()
            var date = start
            while (!date.isAfter(end)) {
                val scheduledHabits = habits.count { isHabitScheduledOn(it, date) }
                val completedHabits = (habitCompletedByDay[date.toEpochDay()] ?: 0).coerceAtMost(scheduledHabits)
                val totalTasks = taskTotalByDay[date.toEpochDay()] ?: 0
                val completedTasks = taskCompletedByDay[date.toEpochDay()] ?: 0
                val total = scheduledHabits + totalTasks
                if (total > 0) {
                    val completed = completedHabits + completedTasks
                    result[date] = (completed.toFloat() / total).coerceIn(0f, 1f)
                }
                date = date.plusDays(1)
            }
            _intensityByDate.value = result
        }
    }

    private fun isHabitScheduledOn(habit: Habit, date: LocalDate): Boolean = when (habit.scheduleType) {
        HabitScheduleType.DAILY, HabitScheduleType.WEEKLY, HabitScheduleType.MONTHLY -> true
        HabitScheduleType.CUSTOM -> habit.customDaysOfWeek.contains(date.dayOfWeek.value)
    }
}
