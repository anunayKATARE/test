package com.lifeos.app.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.goal.domain.GoalStatus
import com.lifeos.app.feature.habit.domain.CalculateHabitStatsUseCase
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.journal.domain.JournalRepository
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.MoodRepository
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HabitCompletionStat(val title: String, val completionRate: Float, val currentStreak: Int)

data class AnalyticsUiState(
    val emotionFrequency: Map<Emotion, Int> = emptyMap(),
    val commonTriggers: Map<String, Int> = emptyMap(),
    val habitStats: List<HabitCompletionStat> = emptyList(),
    val goalsByStatus: Map<GoalStatus, Int> = emptyMap(),
    val totalJournalEntries: Int = 0,
    val taskTriggerFailures: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val journalRepository: JournalRepository,
    private val taskRepository: TaskRepository,
    private val calculateHabitStatsUseCase: CalculateHabitStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val end = Instant.now()
            val start = end.minus(90, ChronoUnit.DAYS)

            val emotionFrequency = moodRepository.emotionFrequency(start, end)
            val commonTriggers = moodRepository.mostCommonTriggers(5)
            val habits = habitRepository.observeActiveHabits().first()
            val habitStats = habits.map { habit ->
                val stats = calculateHabitStatsUseCase(habit)
                HabitCompletionStat(habit.title, stats.completionRate, stats.currentStreak)
            }
            val goals = goalRepository.observeAllGoals().first()
            val goalsByStatus = goals.groupingBy { it.status }.eachCount()
            val journalEntries = journalRepository.observeAll().first()
            val taskTriggerFailures = taskRepository.getTriggerFailureCounts()

            _uiState.value = AnalyticsUiState(
                emotionFrequency = emotionFrequency,
                commonTriggers = commonTriggers,
                habitStats = habitStats,
                goalsByStatus = goalsByStatus,
                totalJournalEntries = journalEntries.size,
                taskTriggerFailures = taskTriggerFailures,
                isLoading = false,
            )
        }
    }
}
