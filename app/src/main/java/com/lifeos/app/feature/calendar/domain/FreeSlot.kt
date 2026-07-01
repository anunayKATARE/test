package com.lifeos.app.feature.calendar.domain

import java.time.Duration
import java.time.Instant

data class FreeSlot(val start: Instant, val end: Instant) {
    val durationMinutes: Long get() = Duration.between(start, end).toMinutes()
}
