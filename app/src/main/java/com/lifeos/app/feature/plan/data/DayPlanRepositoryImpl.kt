package com.lifeos.app.feature.plan.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.plan.domain.DayPlan
import com.lifeos.app.feature.plan.domain.DayPlanRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DayPlanRepositoryImpl @Inject constructor(
    private val dao: DayPlanDao,
    private val dataStore: DataStore<Preferences>,
    private val demoModeRepository: DemoModeRepository,
) : DayPlanRepository {

    override fun observePlanForDate(date: LocalDate): Flow<DayPlan?> =
        combine(dao.observeForDate(date.toEpochDay()), demoModeRepository.activeProfile) { entity, profile ->
            if (entity?.profileId == profile?.id) entity?.toDomain() else null
        }

    override suspend fun savePlan(plan: DayPlan) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(plan.toEntity(profileId))
    }

    override suspend fun isPlanCompletedToday(): Boolean {
        val today = LocalDate.now().toString()
        return dataStore.data.first()[PLAN_COMPLETED_DATE_KEY] == today
    }

    override suspend fun markPlanCompleted() {
        dataStore.edit { it[PLAN_COMPLETED_DATE_KEY] = LocalDate.now().toString() }
    }

    override suspend fun snoozeUntil(epochMillis: Long) {
        dataStore.edit { it[PLAN_SNOOZED_UNTIL_KEY] = epochMillis }
    }

    override suspend fun getSnoozedUntil(): Long =
        dataStore.data.first()[PLAN_SNOOZED_UNTIL_KEY] ?: 0L

    override suspend fun deleteAllByProfile(profileId: String) =
        dao.deleteAllByProfile(profileId)

    companion object {
        private val PLAN_COMPLETED_DATE_KEY = stringPreferencesKey("plan_completed_date")
        private val PLAN_SNOOZED_UNTIL_KEY = longPreferencesKey("plan_snoozed_until")
    }
}
