package com.lifeos.app.feature.task.domain

import java.time.Instant
import java.time.LocalDate

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val triggers: List<String> = emptyList(),
)
