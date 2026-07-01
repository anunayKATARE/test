package com.lifeos.app.feature.plan.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.plan.domain.DayPlan
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey val id: String,
    val forDate: LocalDate,
    val plannedAt: Instant,
    val selectedTaskIds: List<String>,
    val intentions: String,
    val profileId: String? = null,
)

fun DayPlanEntity.toDomain() = DayPlan(
    id = id,
    forDate = forDate,
    plannedAt = plannedAt,
    selectedTaskIds = selectedTaskIds,
    intentions = intentions,
)

fun DayPlan.toEntity(profileId: String?) = DayPlanEntity(
    id = id,
    forDate = forDate,
    plannedAt = plannedAt,
    selectedTaskIds = selectedTaskIds,
    intentions = intentions,
    profileId = profileId,
)
