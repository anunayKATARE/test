package com.lifeos.app.feature.inspiration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.inspiration.domain.AddImageUseCase
import com.lifeos.app.feature.inspiration.domain.AddQuoteUseCase
import com.lifeos.app.feature.inspiration.domain.DeleteInspirationItemUseCase
import com.lifeos.app.feature.inspiration.domain.InspirationItem
import com.lifeos.app.feature.inspiration.domain.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class InspirationViewModel @Inject constructor(
    repository: InspirationRepository,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val addImageUseCase: AddImageUseCase,
    private val deleteInspirationItemUseCase: DeleteInspirationItemUseCase,
) : ViewModel() {

    val items: StateFlow<List<InspirationItem>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addQuote(text: String, author: String) {
        viewModelScope.launch { addQuoteUseCase(text, author) }
    }

    fun addImage(imagePath: String) {
        viewModelScope.launch { addImageUseCase(imagePath) }
    }

    fun delete(id: String) {
        viewModelScope.launch { deleteInspirationItemUseCase(id) }
    }
}
