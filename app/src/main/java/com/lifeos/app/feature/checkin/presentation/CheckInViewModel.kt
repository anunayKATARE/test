package com.lifeos.app.feature.checkin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.common.IdGenerator
import com.lifeos.app.feature.checkin.domain.CheckInCommitment
import com.lifeos.app.feature.checkin.domain.CheckInRepository
import com.lifeos.app.feature.checkin.domain.CheckInSession
import com.lifeos.app.feature.checkin.domain.CommitmentType
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckInUiState(
    val activeSession: CheckInSession? = null,
    val showDialog: Boolean = false,
    val todaysTasks: List<Task> = emptyList(),
    val todaysHabits: List<Habit> = emptyList(),
    val todaysGoals: List<Goal> = emptyList(),
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)

    private val dataFlow = combine(
        checkInRepository.observeActive(),
        taskRepository.observeTasksForDate(LocalDate.now()),
        habitRepository.observeActiveHabits(),
        goalRepository.observeGoalsByHorizon(GoalHorizon.TODAY),
    ) { session, tasks, habits, goals ->
        CheckInUiState(
            activeSession = session,
            todaysTasks = tasks.filter { !it.completed },
            todaysHabits = habits,
            todaysGoals = goals,
        )
    }

    val uiState: StateFlow<CheckInUiState> = combine(dataFlow, _showDialog) { data, show ->
        data.copy(showDialog = show)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckInUiState())

    fun openDialog() {
        _showDialog.value = true
    }

    fun dismissDialog() {
        _showDialog.value = false
    }

    fun schedule(scheduledAt: Instant, commitments: List<CheckInCommitment>) {
        viewModelScope.launch {
            checkInRepository.upsert(
                CheckInSession(
                    id = IdGenerator.newId(),
                    scheduledAt = scheduledAt,
                    commitments = commitments,
                ),
            )
            _showDialog.value = false
        }
    }

    fun completeAndScheduleNext(
        completedCommitments: Set<String>,
        nextScheduledAt: Instant?,
        nextCommitments: List<CheckInCommitment>,
    ) {
        viewModelScope.launch {
            val session = uiState.value.activeSession ?: return@launch
            val updated = session.copy(
                commitments = session.commitments.map { c ->
                    c.copy(isCompleted = completedCommitments.contains(c.id))
                },
            )
            checkInRepository.upsert(updated)
            checkInRepository.complete(session.id)
            if (nextScheduledAt != null) {
                checkInRepository.upsert(
                    CheckInSession(
                        id = IdGenerator.newId(),
                        scheduledAt = nextScheduledAt,
                        commitments = nextCommitments,
                    ),
                )
            }
            _showDialog.value = false
        }
    }

    fun skip() {
        viewModelScope.launch {
            val session = uiState.value.activeSession ?: return@launch
            checkInRepository.complete(session.id)
            _showDialog.value = false
        }
    }

    fun buildCommitmentFromTask(task: Task) = CheckInCommitment(
        id = IdGenerator.newId(),
        text = task.title,
        linkedId = task.id,
        linkedType = CommitmentType.TASK,
    )

    fun buildCommitmentFromHabit(habit: Habit) = CheckInCommitment(
        id = IdGenerator.newId(),
        text = habit.title,
        linkedId = habit.id,
        linkedType = CommitmentType.HABIT,
    )

    fun buildCommitmentFromGoal(goal: Goal) = CheckInCommitment(
        id = IdGenerator.newId(),
        text = goal.title,
        linkedId = goal.id,
        linkedType = CommitmentType.GOAL,
    )

    fun buildFreeTextCommitment(text: String) = CheckInCommitment(
        id = IdGenerator.newId(),
        text = text,
    )
}
