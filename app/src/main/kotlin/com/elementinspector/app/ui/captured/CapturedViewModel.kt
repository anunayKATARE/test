package com.elementinspector.app.ui.captured

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elementinspector.domain.model.CaptureSummary
import com.elementinspector.domain.repository.CaptureRepository
import kotlinx.coroutines.launch

class CapturedViewModel(private val repository: CaptureRepository) : ViewModel() {

    private val _captures = MutableLiveData<List<CaptureSummary>>(emptyList())
    val captures: LiveData<List<CaptureSummary>> = _captures

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _captures.value = repository.getAllSummaries()
        }
    }

    class Factory(private val repository: CaptureRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CapturedViewModel(repository) as T
    }
}
