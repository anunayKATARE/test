package com.lifeos.app.feature.mood.data

import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.mood.domain.MoodRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoodRepositoryImpl @Inject constructor(
    private val dao: MoodDao,
) : MoodRepository {

    override fun observeAll(): Flow<List<MoodEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<MoodEntry>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeBetween(start: Instant, end: Instant): Flow<List<MoodEntry>> =
        dao.observeBetween(start.toEpochMilli(), end.toEpochMilli()).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): MoodEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: MoodEntry) = dao.upsert(entry.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun mostCommonTriggers(limit: Int): Map<String, Int> =
        dao.mostCommonTriggers(limit).associate { it.trigger to it.count }

    override suspend fun emotionFrequency(start: Instant, end: Instant): Map<Emotion, Int> =
        dao.emotionFrequency(start.toEpochMilli(), end.toEpochMilli())
            .associate { Emotion.valueOf(it.emotion) to it.count }
}
