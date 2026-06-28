package com.lifeos.app.feature.journal.data

import com.lifeos.app.feature.journal.domain.JournalEntry
import com.lifeos.app.feature.journal.domain.JournalRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao,
) : JournalRepository {

    override fun observeAll(): Flow<List<JournalEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<JournalEntry>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeBetween(start: Instant, end: Instant): Flow<List<JournalEntry>> =
        dao.observeBetween(start.toEpochMilli(), end.toEpochMilli()).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): JournalEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: JournalEntry) = dao.upsert(entry.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun search(query: String): List<JournalEntry> =
        dao.search(query).map { it.toDomain() }

    override suspend fun countOnDatesBetween(start: Instant, end: Instant): Map<Long, Int> =
        dao.countByDay(start.toEpochMilli(), end.toEpochMilli()).associate { it.day to it.count }
}
