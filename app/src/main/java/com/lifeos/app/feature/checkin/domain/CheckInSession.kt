package com.lifeos.app.feature.checkin.domain

import java.time.Instant

data class CheckInSession(
    val id: String,
    val scheduledAt: Instant,
    val commitments: List<CheckInCommitment> = emptyList(),
    val completedAt: Instant? = null,
) {
    val isOverdue: Boolean get() = completedAt == null && scheduledAt <= Instant.now()
    val isPending: Boolean get() = completedAt == null && !isOverdue
}
