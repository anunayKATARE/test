package com.lifeos.app.feature.goal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val horizon: GoalHorizon,
    val targetDate: LocalDate?,
    val status: GoalStatus,
    val linkedHabitIds: List<String>,
    val linkedJournalIds: List<String>,
    val categoryId: String?,
    val createdAt: Instant,
    val completedAt: Instant?,
    val profileId: String? = null,
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    description = description,
    horizon = horizon,
    targetDate = targetDate,
    status = status,
    linkedHabitIds = linkedHabitIds,
    linkedJournalIds = linkedJournalIds,
    categoryId = categoryId,
    createdAt = createdAt,
    completedAt = completedAt,
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    horizon = horizon,
    targetDate = targetDate,
    status = status,
    linkedHabitIds = linkedHabitIds,
    linkedJournalIds = linkedJournalIds,
    categoryId = categoryId,
    createdAt = createdAt,
    completedAt = completedAt,
)
