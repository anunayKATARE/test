package com.lifeos.app.feature.habit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.habit.domain.AddHabitUseCase
import com.lifeos.app.feature.habit.domain.ArchiveHabitUseCase
import com.lifeos.app.feature.habit.domain.CalculateHabitStatsUseCase
import com.lifeos.app.feature.habit.domain.CompleteHabitUseCase
import com.lifeos.app.feature.habit.domain.DeleteHabitUseCase
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.habit.domain.HabitStats
import com.lifeos.app.feature.habit.domain.UncompleteHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitUiModel(
    val habit: Habit,
    val completedToday: Boolean,
    val stats: HabitStats,
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val addHabitUseCase: AddHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val archiveHabitUseCase: ArchiveHabitUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val uncompleteHabitUseCase: UncompleteHabitUseCase,
    private val calculateHabitStatsUseCase: CalculateHabitStatsUseCase,
) : ViewModel() {

    val habits: StateFlow<List<HabitUiModel>> = combine(
        repository.observeActiveHabits(),
        repository.observeCompletionsOn(LocalDate.now()),
    ) { habits, completions ->
        val completedIds = completions.filter { it.completed }.map { it.habitId }.toSet()
        habits.map { habit ->
            HabitUiModel(
                habit = habit,
                completedToday = completedIds.contains(habit.id),
                stats = calculateHabitStatsUseCase(habit),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleCompletion(habit: Habit, completedToday: Boolean) {
        viewModelScope.launch {
            if (completedToday) uncompleteHabitUseCase(habit.id) else completeHabitUseCase(habit.id)
        }
    }

    fun addHabit(
        title: String,
        description: String,
        scheduleType: HabitScheduleType,
        difficulty: HabitDifficulty,
        importance: HabitImportance,
    ) {
        viewModelScope.launch { addHabitUseCase(title, description, scheduleType, emptyList(), difficulty, importance) }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch { deleteHabitUseCase(id) }
    }

    fun archiveHabit(id: String) {
        viewModelScope.launch { archiveHabitUseCase(id, true) }
    }
}
