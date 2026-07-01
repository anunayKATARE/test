package com.lifeos.app.feature.calendar.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun hasPermission(): Boolean
    fun observeEventsForDay(date: LocalDate): Flow<List<CalendarEvent>>
}
