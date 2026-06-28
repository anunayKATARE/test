package com.lifeos.app.feature.inspiration.data

import com.lifeos.app.feature.inspiration.domain.InspirationItem
import com.lifeos.app.feature.inspiration.domain.InspirationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InspirationRepositoryImpl @Inject constructor(
    private val dao: InspirationDao,
) : InspirationRepository {

    override fun observeAll(): Flow<List<InspirationItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(item: InspirationItem) = dao.upsert(item.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
