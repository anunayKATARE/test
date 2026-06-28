package com.lifeos.app.feature.journal.domain

import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun observeAll(): Flow<List<JournalEntry>>
    fun observeRecent(limit: Int): Flow<List<JournalEntry>>
    fun observeBetween(start: Instant, end: Instant): Flow<List<JournalEntry>>
    suspend fun getById(id: String): JournalEntry?
    suspend fun upsert(entry: JournalEntry)
    suspend fun delete(id: String)
    suspend fun search(query: String): List<JournalEntry>
    suspend fun countOnDatesBetween(start: Instant, end: Instant): Map<Long, Int>
}
