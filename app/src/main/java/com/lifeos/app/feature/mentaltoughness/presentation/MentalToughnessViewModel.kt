package com.lifeos.app.feature.mentaltoughness.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.mentaltoughness.domain.DeleteMentalToughnessEntryUseCase
import com.lifeos.app.feature.mentaltoughness.domain.LogMentalToughnessEntryUseCase
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessEntry
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessRepository
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MentalToughnessViewModel @Inject constructor(
    repository: MentalToughnessRepository,
    private val logEntryUseCase: LogMentalToughnessEntryUseCase,
    private val deleteEntryUseCase: DeleteMentalToughnessEntryUseCase,
) : ViewModel() {

    val entries: StateFlow<List<MentalToughnessEntry>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun logEntry(type: MentalToughnessType, title: String, description: String, outcome: String, lessonLearned: String) {
        viewModelScope.launch { logEntryUseCase(type, title, description, outcome = outcome, lessonLearned = lessonLearned) }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { deleteEntryUseCase(id) }
    }
}
