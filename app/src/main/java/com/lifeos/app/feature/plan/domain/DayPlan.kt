package com.lifeos.app.feature.plan.domain

import java.time.Instant
import java.time.LocalDate

data class DayPlan(
    val id: String,
    val forDate: LocalDate,
    val plannedAt: Instant,
    val selectedTaskIds: List<String>,
    val intentions: String = "",
)
