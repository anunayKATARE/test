package com.lifeos.app.feature.mood.domain

import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun observeAll(): Flow<List<MoodEntry>>
    fun observeRecent(limit: Int): Flow<List<MoodEntry>>
    fun observeBetween(start: Instant, end: Instant): Flow<List<MoodEntry>>
    suspend fun getById(id: String): MoodEntry?
    suspend fun upsert(entry: MoodEntry)
    suspend fun delete(id: String)
    suspend fun mostCommonTriggers(limit: Int): Map<String, Int>
    suspend fun emotionFrequency(start: Instant, end: Instant): Map<Emotion, Int>
}
