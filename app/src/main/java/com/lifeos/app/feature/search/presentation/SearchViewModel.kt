package com.lifeos.app.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.functions.AppFunctions
import com.lifeos.app.functions.SearchResults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appFunctions: AppFunctions,
) : ViewModel() {

    private val _results = MutableStateFlow<SearchResults?>(null)
    val results: StateFlow<SearchResults?> = _results.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _results.value = null
            return
        }
        viewModelScope.launch { _results.value = appFunctions.searchEntries(query) }
    }
}
