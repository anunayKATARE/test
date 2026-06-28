package com.lifeos.app.feature.goal.domain

import java.time.Instant
import java.time.LocalDate

enum class GoalHorizon { TODAY, WEEKLY, MONTHLY, QUARTERLY, LONG_TERM }
enum class GoalStatus { ACTIVE, COMPLETED, ARCHIVED }

data class Goal(
    val id: String,
    val title: String,
    val description: String = "",
    val horizon: GoalHorizon = GoalHorizon.WEEKLY,
    val targetDate: LocalDate? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val linkedHabitIds: List<String> = emptyList(),
    val linkedJournalIds: List<String> = emptyList(),
    val categoryId: String? = null,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
)
