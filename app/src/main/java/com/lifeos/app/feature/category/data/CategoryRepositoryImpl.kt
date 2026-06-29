package com.lifeos.app.feature.category.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.category.domain.Category
import com.lifeos.app.feature.category.domain.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val demoModeRepository: DemoModeRepository,
) : CategoryRepository {

    private fun Flow<List<CategoryEntity>>.filterByActiveProfile(): Flow<List<CategoryEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeVisibleCategories(): Flow<List<Category>> =
        dao.observeVisible().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeAllCategories(): Flow<List<Category>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(id: String): Category? = dao.getById(id)?.toDomain()

    override suspend fun upsert(category: Category) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(category.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived)

    override suspend fun setHidden(id: String, hidden: Boolean) = dao.setHidden(id, hidden)

    override suspend fun reorder(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id -> dao.setOrderIndex(id, index) }
    }
}
