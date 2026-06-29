package com.lifeos.app.feature.reflection.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.reflection.domain.ReflectionEntry
import com.lifeos.app.feature.reflection.domain.ReflectionRepository
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ReflectionRepositoryImpl @Inject constructor(
    private val dao: ReflectionDao,
    private val demoModeRepository: DemoModeRepository,
) : ReflectionRepository {

    private fun Flow<List<ReflectionEntity>>.filterByActiveProfile(): Flow<List<ReflectionEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeAll(): Flow<List<ReflectionEntry>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: ReflectionTemplateType): Flow<List<ReflectionEntry>> =
        dao.observeByType(type).filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): ReflectionEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: ReflectionEntry) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(entry.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)
}
