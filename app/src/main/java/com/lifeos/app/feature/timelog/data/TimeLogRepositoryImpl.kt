package com.lifeos.app.feature.timelog.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.timelog.domain.TimeLog
import com.lifeos.app.feature.timelog.domain.TimeLogRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

class TimeLogRepositoryImpl @Inject constructor(
    private val dao: TimeLogDao,
    private val dataStore: DataStore<Preferences>,
    private val demoModeRepository: DemoModeRepository,
) : TimeLogRepository {

    override fun observeLogsForDay(date: LocalDate): Flow<List<TimeLog>> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return combine(
            dao.observeForDay(startMillis, endMillis),
            demoModeRepository.activeProfile,
        ) { entities, profile ->
            entities.filter { it.profileId == profile?.id }.map { it.toDomain() }
        }
    }

    override fun observeActiveTimer(): Flow<TimeLog?> =
        combine(dao.observeActiveTimer(), demoModeRepository.activeProfile) { entity, profile ->
            if (entity?.profileId == profile?.id) entity?.toDomain() else null
        }

    override suspend fun startTimer(linkedTaskId: String?, chore: String?): TimeLog {
        val profileId = demoModeRepository.activeProfile.first()?.id
        val log = TimeLog(
            id = UUID.randomUUID().toString(),
            startedAt = Instant.now(),
            linkedTaskId = linkedTaskId,
            chore = chore,
            profileId = profileId,
        )
        dao.upsert(log.toEntity())
        touchLastEntryMillis()
        return log
    }

    override suspend fun stopTimer(logId: String) {
        dao.stopTimer(logId, Instant.now().toEpochMilli())
        touchLastEntryMillis()
    }

    override suspend fun saveLog(log: TimeLog) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(log.toEntity().copy(profileId = profileId))
        touchLastEntryMillis()
    }

    override suspend fun deleteLog(id: String) = dao.delete(id)

    override suspend fun getRecentChores(): List<String> {
        val profileId = demoModeRepository.activeProfile.first()?.id ?: return emptyList()
        return dao.getRecentChores(profileId).filterNotNull()
    }

    override suspend fun getLastEntryMillis(): Long =
        dataStore.data.first()[LAST_TIME_LOG_ENTRY_KEY] ?: 0L

    override suspend fun touchLastEntryMillis() {
        dataStore.edit { it[LAST_TIME_LOG_ENTRY_KEY] = System.currentTimeMillis() }
    }

    override suspend fun getFirstOpenMillis(): Long =
        dataStore.data.first()[FIRST_OPEN_KEY] ?: 0L

    override suspend fun setFirstOpenMillis(millis: Long) {
        dataStore.edit { it[FIRST_OPEN_KEY] = millis }
    }

    override fun observeLoggingEnabled(): Flow<Boolean> =
        dataStore.data.map { it[LOGGING_ENABLED_KEY] ?: true }.distinctUntilChanged()

    override suspend fun setLoggingEnabled(enabled: Boolean) {
        dataStore.edit { it[LOGGING_ENABLED_KEY] = enabled }
    }

    override suspend fun deleteAllByProfile(profileId: String) = dao.deleteAllByProfile(profileId)

    companion object {
        private val LAST_TIME_LOG_ENTRY_KEY = longPreferencesKey("last_time_log_entry")
        private val FIRST_OPEN_KEY = longPreferencesKey("first_open_time")
        private val LOGGING_ENABLED_KEY = booleanPreferencesKey("time_log_enabled")
    }
}
