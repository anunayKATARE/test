package com.lifeos.app.feature.timelog.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TimeLogRepository {
    fun observeLogsForDay(date: LocalDate): Flow<List<TimeLog>>
    fun observeActiveTimer(): Flow<TimeLog?>
    suspend fun startTimer(linkedTaskId: String?, chore: String?): TimeLog
    suspend fun stopTimer(logId: String)
    suspend fun saveLog(log: TimeLog)
    suspend fun deleteLog(id: String)
    suspend fun getRecentChores(): List<String>
    suspend fun getLastEntryMillis(): Long
    suspend fun touchLastEntryMillis()
    suspend fun getFirstOpenMillis(): Long
    suspend fun setFirstOpenMillis(millis: Long)
    suspend fun deleteAllByProfile(profileId: String)
}
