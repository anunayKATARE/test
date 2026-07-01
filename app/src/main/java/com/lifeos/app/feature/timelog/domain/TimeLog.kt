package com.lifeos.app.feature.timelog.domain

import java.time.Instant

data class TimeLog(
    val id: String,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val linkedTaskId: String? = null,
    val chore: String? = null,
    val profileId: String? = null,
) {
    val isActive: Boolean get() = endedAt == null
    val label: String get() = chore ?: linkedTaskId ?: "Unknown"
}
