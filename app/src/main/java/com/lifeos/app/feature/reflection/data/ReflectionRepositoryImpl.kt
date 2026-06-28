package com.lifeos.app.feature.reflection.data

import com.lifeos.app.feature.reflection.domain.ReflectionEntry
import com.lifeos.app.feature.reflection.domain.ReflectionRepository
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReflectionRepositoryImpl @Inject constructor(
    private val dao: ReflectionDao,
) : ReflectionRepository {

    override fun observeAll(): Flow<List<ReflectionEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: ReflectionTemplateType): Flow<List<ReflectionEntry>> =
        dao.observeByType(type).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): ReflectionEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: ReflectionEntry) = dao.upsert(entry.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)
}
