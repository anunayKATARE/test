package com.lifeos.app.feature.mentaltoughness.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessEntry
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessRepository
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MentalToughnessRepositoryImpl @Inject constructor(
    private val dao: MentalToughnessDao,
    private val demoModeRepository: DemoModeRepository,
) : MentalToughnessRepository {

    private fun Flow<List<MentalToughnessEntity>>.filterByActiveProfile(): Flow<List<MentalToughnessEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeAll(): Flow<List<MentalToughnessEntry>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: MentalToughnessType): Flow<List<MentalToughnessEntry>> =
        dao.observeByType(type).filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): MentalToughnessEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: MentalToughnessEntry) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(entry.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun countByType(): Map<MentalToughnessType, Int> =
        dao.countByType().associate { it.type to it.count }
}
