package com.elementinspector.app.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elementinspector.domain.model.CaptureDetail
import com.elementinspector.domain.repository.CaptureRepository
import kotlinx.coroutines.launch

class CaptureDetailViewModel(
    private val repository: CaptureRepository,
    private val captureId: String,
) : ViewModel() {

    private val _detail = MutableLiveData<CaptureDetail?>()
    val detail: LiveData<CaptureDetail?> = _detail

    init {
        viewModelScope.launch {
            _detail.value = repository.getDetail(captureId)
        }
    }

    class Factory(
        private val repository: CaptureRepository,
        private val captureId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CaptureDetailViewModel(repository, captureId) as T
    }
}
