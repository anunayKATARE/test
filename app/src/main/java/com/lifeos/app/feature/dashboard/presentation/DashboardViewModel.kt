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
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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
    val openProblemsCount: Int = 0,
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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weeksBack = 12
            val rangeStart = today.minusDays((weeksBack * 7 - 1).toLong())

            val goals = getTodaysGoalsUseCase()
            val habits = getTodaysHabitsUseCase()
            val todaysCompletions = habitRepository.observeCompletionsOn(today).first()
            val completedIds = todaysCompletions.filter { it.completed }.map { it.habitId }.toSet()

            val moods = moodRepository.observeRecent(5).first()
            val openProblems = problemRepository.observeOpen().first()

            val habitCompletionsByDay = habitRepository.completionCountByDay(rangeStart, today)
            val journalCountsByDay = journalRepository.countOnDatesBetween(
                rangeStart.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
                today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
            )
            val heatmap = (habitCompletionsByDay.keys + journalCountsByDay.keys).associate { epochDay ->
                val date = LocalDate.ofEpochDay(epochDay)
                date to ((habitCompletionsByDay[epochDay] ?: 0) + (journalCountsByDay[epochDay] ?: 0))
            }

            _uiState.value = DashboardUiState(
                todaysGoals = goals,
                todaysHabits = habits.map { TodayHabitUiModel(it, completedIds.contains(it.id)) },
                recentMoodEntries = moods,
                openProblemsCount = openProblems.size,
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
