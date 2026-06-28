package com.lifeos.app.feature.goal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.goal.domain.AddGoalUseCase
import com.lifeos.app.feature.goal.domain.ArchiveGoalUseCase
import com.lifeos.app.feature.goal.domain.CompleteGoalUseCase
import com.lifeos.app.feature.goal.domain.DeleteGoalUseCase
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GoalViewModel @Inject constructor(
    repository: GoalRepository,
    private val addGoalUseCase: AddGoalUseCase,
    private val completeGoalUseCase: CompleteGoalUseCase,
    private val archiveGoalUseCase: ArchiveGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = repository.observeActiveGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGoal(title: String, description: String, horizon: GoalHorizon) {
        viewModelScope.launch { addGoalUseCase(title = title, description = description, horizon = horizon) }
    }

    fun completeGoal(id: String) {
        viewModelScope.launch { completeGoalUseCase(id) }
    }

    fun archiveGoal(id: String) {
        viewModelScope.launch { archiveGoalUseCase(id) }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { deleteGoalUseCase(id) }
    }
}
