package com.lifeos.app.feature.category.domain

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeVisibleCategories(): Flow<List<Category>>
    fun observeAllCategories(): Flow<List<Category>>
    suspend fun getCategory(id: String): Category?
    suspend fun upsert(category: Category)
    suspend fun delete(id: String)
    suspend fun setArchived(id: String, archived: Boolean)
    suspend fun setHidden(id: String, hidden: Boolean)
    suspend fun reorder(orderedIds: List<String>)
}
