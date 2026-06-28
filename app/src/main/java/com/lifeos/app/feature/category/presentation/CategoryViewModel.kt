package com.lifeos.app.feature.category.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.category.domain.ArchiveCategoryUseCase
import com.lifeos.app.feature.category.domain.Category
import com.lifeos.app.feature.category.domain.CategoryRepository
import com.lifeos.app.feature.category.domain.DeleteCategoryUseCase
import com.lifeos.app.feature.category.domain.EditCategoryUseCase
import com.lifeos.app.feature.category.domain.HideCategoryUseCase
import com.lifeos.app.feature.category.domain.AddCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryViewModel @Inject constructor(
    repository: CategoryRepository,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val editCategoryUseCase: EditCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val archiveCategoryUseCase: ArchiveCategoryUseCase,
    private val hideCategoryUseCase: HideCategoryUseCase,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCategory(name: String, description: String, colorHex: String, icon: String) {
        viewModelScope.launch { addCategoryUseCase(name, description, colorHex, icon) }
    }

    fun editCategory(category: Category) {
        viewModelScope.launch { editCategoryUseCase(category) }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch { deleteCategoryUseCase(id) }
    }

    fun setArchived(id: String, archived: Boolean) {
        viewModelScope.launch { archiveCategoryUseCase(id, archived) }
    }

    fun setHidden(id: String, hidden: Boolean) {
        viewModelScope.launch { hideCategoryUseCase(id, hidden) }
    }
}
