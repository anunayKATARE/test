package com.lifeos.app.feature.journal.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.journal.domain.JournalEntry
import com.lifeos.app.feature.journal.domain.JournalRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao,
    private val demoModeRepository: DemoModeRepository,
) : JournalRepository {

    private fun Flow<List<JournalEntryEntity>>.filterByActiveProfile(): Flow<List<JournalEntryEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeAll(): Flow<List<JournalEntry>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<JournalEntry>> =
        dao.observeRecent(limit).filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeBetween(start: Instant, end: Instant): Flow<List<JournalEntry>> =
        dao.observeBetween(start.toEpochMilli(), end.toEpochMilli()).filterByActiveProfile()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): JournalEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: JournalEntry) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(entry.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun search(query: String): List<JournalEntry> =
        dao.search(query).map { it.toDomain() }

    override suspend fun countOnDatesBetween(start: Instant, end: Instant): Map<Long, Int> =
        dao.countByDay(start.toEpochMilli(), end.toEpochMilli()).associate { it.day to it.count }
}
