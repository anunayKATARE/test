package com.lifeos.app.feature.calendar.domain

import java.time.LocalDate

interface CalendarRepository {
    fun hasPermission(): Boolean
    suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent>
}
