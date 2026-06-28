package com.lifeos.app.feature.selfbelief.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.selfbelief.domain.AddSelfBeliefReflectionUseCase
import com.lifeos.app.feature.selfbelief.domain.BuildStrengthsEvidenceBaseUseCase
import com.lifeos.app.feature.selfbelief.domain.DeleteSelfBeliefReflectionUseCase
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefReflection
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SelfBeliefViewModel @Inject constructor(
    repository: SelfBeliefRepository,
    private val addReflectionUseCase: AddSelfBeliefReflectionUseCase,
    private val deleteReflectionUseCase: DeleteSelfBeliefReflectionUseCase,
    private val buildStrengthsEvidenceBaseUseCase: BuildStrengthsEvidenceBaseUseCase,
) : ViewModel() {

    val reflections: StateFlow<List<SelfBeliefReflection>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _strengthsEvidenceBase = MutableStateFlow<List<String>>(emptyList())
    val strengthsEvidenceBase: StateFlow<List<String>> = _strengthsEvidenceBase.asStateFlow()

    init {
        viewModelScope.launch { _strengthsEvidenceBase.value = buildStrengthsEvidenceBaseUseCase() }
    }

    fun addReflection(
        whatHappened: String,
        storyTelling: String,
        evidenceFor: String,
        evidenceAgainst: String,
        friendAdvice: String,
        strengthsThatRemain: String,
        nextSmallAction: String,
    ) {
        viewModelScope.launch {
            addReflectionUseCase(whatHappened, storyTelling, evidenceFor, evidenceAgainst, friendAdvice, strengthsThatRemain, nextSmallAction)
            _strengthsEvidenceBase.value = buildStrengthsEvidenceBaseUseCase()
        }
    }

    fun deleteReflection(id: String) {
        viewModelScope.launch { deleteReflectionUseCase(id) }
    }
}
