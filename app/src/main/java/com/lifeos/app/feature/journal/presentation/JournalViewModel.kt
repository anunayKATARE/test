package com.lifeos.app.feature.journal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.journal.domain.CreateJournalEntryUseCase
import com.lifeos.app.feature.journal.domain.DeleteJournalEntryUseCase
import com.lifeos.app.feature.journal.domain.JournalEntry
import com.lifeos.app.feature.journal.domain.JournalRepository
import com.lifeos.app.feature.journal.domain.SearchJournalEntriesUseCase
import com.lifeos.app.feature.journal.domain.UpdateJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class JournalViewModel @Inject constructor(
    repository: JournalRepository,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val searchJournalEntriesUseCase: SearchJournalEntriesUseCase,
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchResults = MutableStateFlow<List<JournalEntry>?>(null)
    val searchResults: StateFlow<List<JournalEntry>?> = _searchResults.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        viewModelScope.launch { _searchResults.value = searchJournalEntriesUseCase(query) }
    }

    fun createEntry(title: String, body: String, tags: List<String>, location: String?, weather: String?) {
        viewModelScope.launch { createJournalEntryUseCase(title, body, tags, location = location, weather = weather) }
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch { updateJournalEntryUseCase(entry) }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { deleteJournalEntryUseCase(id) }
    }
}
