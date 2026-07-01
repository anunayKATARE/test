package com.lifeos.app.feature.calendar.domain

import java.time.Instant

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startAt: Instant,
    val endAt: Instant,
    val isAllDay: Boolean,
    val calendarId: Long,
)
