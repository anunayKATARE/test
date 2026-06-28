package com.lifeos.app.feature.reflection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.reflection.domain.DeleteReflectionEntryUseCase
import com.lifeos.app.feature.reflection.domain.ReflectionEntry
import com.lifeos.app.feature.reflection.domain.ReflectionRepository
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import com.lifeos.app.feature.reflection.domain.SaveReflectionEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    repository: ReflectionRepository,
    private val saveReflectionEntryUseCase: SaveReflectionEntryUseCase,
    private val deleteReflectionEntryUseCase: DeleteReflectionEntryUseCase,
) : ViewModel() {

    val entries: StateFlow<List<ReflectionEntry>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveEntry(templateType: ReflectionTemplateType, title: String, answers: Map<String, String>) {
        viewModelScope.launch { saveReflectionEntryUseCase(templateType, title, answers) }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { deleteReflectionEntryUseCase(id) }
    }
}
