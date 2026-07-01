package com.lifeos.app.feature.plan.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DayPlanRepository {
    fun observePlanForDate(date: LocalDate): Flow<DayPlan?>
    suspend fun savePlan(plan: DayPlan)
    suspend fun isPlanCompletedToday(): Boolean
    suspend fun markPlanCompleted()
    suspend fun snoozeUntil(epochMillis: Long)
    suspend fun getSnoozedUntil(): Long
    suspend fun deleteAllByProfile(profileId: String)
}
