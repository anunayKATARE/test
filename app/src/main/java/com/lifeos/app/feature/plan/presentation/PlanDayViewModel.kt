package com.lifeos.app.feature.plan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.plan.domain.DayPlan
import com.lifeos.app.feature.plan.domain.DayPlanRepository
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanDayUiState(
    val tomorrowsTasks: List<Task> = emptyList(),
    val selectedTaskIds: Set<String> = emptySet(),
    val intentions: String = "",
    val existingPlan: DayPlan? = null,
)

@HiltViewModel
class PlanDayViewModel @Inject constructor(
    private val planRepository: DayPlanRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val tomorrow = LocalDate.now().plusDays(1)

    private val _selectedTaskIds = MutableStateFlow<Set<String>>(emptySet())
    private val _intentions = MutableStateFlow("")

    val uiState: StateFlow<PlanDayUiState> = combine(
        taskRepository.observeTasksForDate(tomorrow),
        _selectedTaskIds,
        _intentions,
        planRepository.observePlanForDate(tomorrow),
    ) { tasks, selected, intentions, existingPlan ->
        PlanDayUiState(
            tomorrowsTasks = tasks,
            selectedTaskIds = selected,
            intentions = intentions,
            existingPlan = existingPlan,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanDayUiState())

    init {
        viewModelScope.launch {
            planRepository.observePlanForDate(tomorrow).collect { plan ->
                if (plan != null) {
                    _selectedTaskIds.value = plan.selectedTaskIds.toSet()
                    _intentions.value = plan.intentions
                }
            }
        }
    }

    fun toggleTask(taskId: String) {
        _selectedTaskIds.value = _selectedTaskIds.value.let {
            if (taskId in it) it - taskId else it + taskId
        }
    }

    fun updateIntentions(text: String) { _intentions.value = text }

    fun savePlan() {
        viewModelScope.launch {
            val existing = uiState.value.existingPlan
            val plan = DayPlan(
                id = existing?.id ?: UUID.randomUUID().toString(),
                forDate = tomorrow,
                plannedAt = Instant.now(),
                selectedTaskIds = _selectedTaskIds.value.toList(),
                intentions = _intentions.value.trim(),
            )
            planRepository.savePlan(plan)
            planRepository.markPlanCompleted()
        }
    }
}
