package com.lifeos.app.feature.category.domain

import com.lifeos.app.core.common.IdGenerator
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(
        name: String,
        description: String = "",
        colorHex: String = "#1B5E20",
        icon: String = "star",
        orderIndex: Int = 0,
    ): Category {
        val category = Category(
            id = IdGenerator.newId(),
            name = name,
            description = description,
            colorHex = colorHex,
            icon = icon,
            orderIndex = orderIndex,
        )
        repository.upsert(category)
        return category
    }
}

class EditCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category) {
        repository.upsert(category)
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String) {
        repository.delete(categoryId)
    }
}

class ArchiveCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String, archived: Boolean = true) {
        repository.setArchived(categoryId, archived)
    }
}

class HideCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String, hidden: Boolean = true) {
        repository.setHidden(categoryId, hidden)
    }
}

class ReorderCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(orderedIds: List<String>) {
        repository.reorder(orderedIds)
    }
}
