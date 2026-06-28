package com.lifeos.app.feature.problemsolver.domain

import java.time.Instant

enum class ProblemStatus { OPEN, IN_PROGRESS, RESOLVED }

data class Problem(
    val id: String,
    val title: String,
    val description: String = "",
    val possibleCauses: List<String> = emptyList(),
    val attemptsMade: List<String> = emptyList(),
    val whatWorked: String = "",
    val whatFailed: String = "",
    val status: ProblemStatus = ProblemStatus.OPEN,
    val notes: String = "",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
