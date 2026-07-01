package com.lifeos.app.feature.checkin.domain

enum class CommitmentType { TASK, HABIT, GOAL }

data class CheckInCommitment(
    val id: String,
    val text: String,
    val linkedId: String? = null,
    val linkedType: CommitmentType? = null,
    val isCompleted: Boolean = false,
)
