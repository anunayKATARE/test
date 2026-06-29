package com.lifeos.app.feature.inspiration.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.inspiration.domain.InspirationItem
import com.lifeos.app.feature.inspiration.domain.InspirationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class InspirationRepositoryImpl @Inject constructor(
    private val dao: InspirationDao,
    private val demoModeRepository: DemoModeRepository,
) : InspirationRepository {

    override fun observeAll(): Flow<List<InspirationItem>> =
        dao.observeAll()
            .combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(item: InspirationItem) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(item.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.delete(id)
}
