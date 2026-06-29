package com.lifeos.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.goal.domain.GetTodaysGoalsUseCase
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.habit.domain.CompleteHabitUseCase
import com.lifeos.app.feature.habit.domain.GetTodaysHabitsUseCase
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.UncompleteHabitUseCase
import com.lifeos.app.feature.journal.domain.JournalRepository
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.mood.domain.MoodRepository
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TodayHabitUiModel(val habit: Habit, val completedToday: Boolean)

data class DashboardUiState(
    val todaysGoals: List<Goal> = emptyList(),
    val todaysHabits: List<TodayHabitUiModel> = emptyList(),
    val recentMoodEntries: List<MoodEntry> = emptyList(),
    val openProblems: List<Problem> = emptyList(),
    val heatmapMonth: YearMonth = YearMonth.now(),
    val heatmap: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTodaysGoalsUseCase: GetTodaysGoalsUseCase,
    private val getTodaysHabitsUseCase: GetTodaysHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val uncompleteHabitUseCase: UncompleteHabitUseCase,
    private val habitRepository: HabitRepository,
    private val moodRepository: MoodRepository,
    private val journalRepository: JournalRepository,
    private val problemRepository: ProblemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var heatmapMonth = YearMonth.now()

    init {
        refresh()
    }

    fun nextMonth() {
        heatmapMonth = heatmapMonth.plusMonths(1)
        refresh()
    }

    fun previousMonth() {
        heatmapMonth = heatmapMonth.minusMonths(1)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val rangeStart = heatmapMonth.atDay(1)
            val rangeEnd = heatmapMonth.atEndOfMonth()

            val goals = getTodaysGoalsUseCase()
            val habits = getTodaysHabitsUseCase()
            val todaysCompletions = habitRepository.observeCompletionsOn(today).first()
            val completedIds = todaysCompletions.filter { it.completed }.map { it.habitId }.toSet()

            val moods = moodRepository.observeRecent(5).first()
            val openProblems = problemRepository.observeOpen().first()

            val habitCompletionsByDay = habitRepository.completionCountByDay(rangeStart, rangeEnd)
            val journalCountsByDay = journalRepository.countOnDatesBetween(
                rangeStart.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
                rangeEnd.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
            )
            val heatmap = (habitCompletionsByDay.keys + journalCountsByDay.keys).associate { epochDay ->
                val date = LocalDate.ofEpochDay(epochDay)
                date to ((habitCompletionsByDay[epochDay] ?: 0) + (journalCountsByDay[epochDay] ?: 0))
            }

            _uiState.value = DashboardUiState(
                todaysGoals = goals,
                todaysHabits = habits.map { TodayHabitUiModel(it, completedIds.contains(it.id)) },
                recentMoodEntries = moods,
                openProblems = openProblems,
                heatmapMonth = heatmapMonth,
                heatmap = heatmap,
                isLoading = false,
            )
        }
    }

    fun toggleHabit(habit: Habit, completedToday: Boolean) {
        viewModelScope.launch {
            if (completedToday) uncompleteHabitUseCase(habit.id) else completeHabitUseCase(habit.id)
            refresh()
        }
    }
}
