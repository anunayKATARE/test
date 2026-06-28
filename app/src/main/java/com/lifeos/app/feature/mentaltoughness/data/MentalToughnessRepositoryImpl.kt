package com.lifeos.app.feature.mentaltoughness.data

import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessEntry
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessRepository
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MentalToughnessRepositoryImpl @Inject constructor(
    private val dao: MentalToughnessDao,
) : MentalToughnessRepository {

    override fun observeAll(): Flow<List<MentalToughnessEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: MentalToughnessType): Flow<List<MentalToughnessEntry>> =
        dao.observeByType(type).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): MentalToughnessEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: MentalToughnessEntry) = dao.upsert(entry.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun countByType(): Map<MentalToughnessType, Int> =
        dao.countByType().associate { it.type to it.count }
}
