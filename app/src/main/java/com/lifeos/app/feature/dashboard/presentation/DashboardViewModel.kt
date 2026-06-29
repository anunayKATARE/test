package com.lifeos.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.habit.domain.CompleteHabitUseCase
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.habit.domain.UncompleteHabitUseCase
import com.lifeos.app.feature.journal.domain.JournalRepository
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.mood.domain.MoodRepository
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayHabitUiModel(val habit: Habit, val completedToday: Boolean)

private data class HeatmapState(val month: YearMonth, val heatmap: Map<LocalDate, Int>)

data class DashboardUiState(
    val todaysGoals: List<Goal> = emptyList(),
    val todaysHabits: List<TodayHabitUiModel> = emptyList(),
    val recentMoodEntries: List<MoodEntry> = emptyList(),
    val openProblems: List<Problem> = emptyList(),
    val heatmapMonth: YearMonth = YearMonth.now(),
    val heatmap: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val uncompleteHabitUseCase: UncompleteHabitUseCase,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val moodRepository: MoodRepository,
    private val journalRepository: JournalRepository,
    private val problemRepository: ProblemRepository,
) : ViewModel() {

    private val heatmapMonth = MutableStateFlow(YearMonth.now())

    private val todaysHabits = combine(
        habitRepository.observeActiveHabits(),
        habitRepository.observeCompletionsOn(LocalDate.now()),
    ) { habits, completions ->
        val today = LocalDate.now()
        val completedIds = completions.filter { it.completed }.map { it.habitId }.toSet()
        habits
            .filter { habit ->
                when (habit.scheduleType) {
                    HabitScheduleType.DAILY, HabitScheduleType.WEEKLY, HabitScheduleType.MONTHLY -> true
                    HabitScheduleType.CUSTOM -> habit.customDaysOfWeek.contains(today.dayOfWeek.value)
                }
            }
            .map { habit -> TodayHabitUiModel(habit, completedIds.contains(habit.id)) }
    }

    private val heatmapState = heatmapMonth.flatMapLatest { month ->
        flow {
            val rangeStart = month.atDay(1)
            val rangeEnd = month.atEndOfMonth()
            val habitCompletionsByDay = habitRepository.completionCountByDay(rangeStart, rangeEnd)
            val journalCountsByDay = journalRepository.countOnDatesBetween(
                rangeStart.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                rangeEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            )
            val heatmap = (habitCompletionsByDay.keys + journalCountsByDay.keys).associate { epochDay ->
                val date = LocalDate.ofEpochDay(epochDay)
                date to ((habitCompletionsByDay[epochDay] ?: 0) + (journalCountsByDay[epochDay] ?: 0))
            }
            emit(HeatmapState(month, heatmap))
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        goalRepository.observeGoalsByHorizon(GoalHorizon.TODAY),
        todaysHabits,
        moodRepository.observeRecent(5),
        problemRepository.observeOpen(),
        heatmapState,
    ) { goals, habits, moods, openProblems, heatmap ->
        DashboardUiState(
            todaysGoals = goals,
            todaysHabits = habits,
            recentMoodEntries = moods,
            openProblems = openProblems,
            heatmapMonth = heatmap.month,
            heatmap = heatmap.heatmap,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun nextMonth() {
        heatmapMonth.value = heatmapMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        heatmapMonth.value = heatmapMonth.value.minusMonths(1)
    }

    fun toggleHabit(habit: Habit, completedToday: Boolean) {
        viewModelScope.launch {
            if (completedToday) uncompleteHabitUseCase(habit.id) else completeHabitUseCase(habit.id)
        }
    }
}
