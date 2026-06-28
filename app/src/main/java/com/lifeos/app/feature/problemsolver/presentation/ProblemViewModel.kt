package com.lifeos.app.feature.problemsolver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.problemsolver.domain.DeleteProblemUseCase
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import com.lifeos.app.feature.problemsolver.domain.RecordAttemptUseCase
import com.lifeos.app.feature.problemsolver.domain.RegisterProblemUseCase
import com.lifeos.app.feature.problemsolver.domain.SetProblemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProblemViewModel @Inject constructor(
    repository: ProblemRepository,
    private val registerProblemUseCase: RegisterProblemUseCase,
    private val recordAttemptUseCase: RecordAttemptUseCase,
    private val setProblemStatusUseCase: SetProblemStatusUseCase,
    private val deleteProblemUseCase: DeleteProblemUseCase,
) : ViewModel() {

    val problems: StateFlow<List<Problem>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun registerProblem(title: String, description: String) {
        viewModelScope.launch { registerProblemUseCase(title, description) }
    }

    fun recordAttempt(problemId: String, attempt: String, worked: Boolean?) {
        viewModelScope.launch { recordAttemptUseCase(problemId, attempt, worked) }
    }

    fun setStatus(id: String, status: ProblemStatus) {
        viewModelScope.launch { setProblemStatusUseCase(id, status) }
    }

    fun deleteProblem(id: String) {
        viewModelScope.launch { deleteProblemUseCase(id) }
    }
}
