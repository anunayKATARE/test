package com.lifeos.app.feature.calendar.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class AvailabilityService @Inject constructor() {

    fun findFreeSlots(
        events: List<CalendarEvent>,
        date: LocalDate,
        windowStartHour: Int = 8,
        windowEndHour: Int = 22,
        minSlotMinutes: Long = 30,
    ): List<FreeSlot> {
        val zone = ZoneId.systemDefault()
        val windowStart = date.atTime(windowStartHour, 0).atZone(zone).toInstant()
        val windowEnd = date.atTime(windowEndHour, 0).atZone(zone).toInstant()

        val busyPeriods = events
            .filter { !it.isAllDay }
            .mapNotNull { event ->
                val s = maxOf(event.startAt, windowStart)
                val e = minOf(event.endAt, windowEnd)
                if (s < e) s to e else null
            }
            .sortedBy { it.first }
            .let { mergePeriods(it) }

        val freeSlots = mutableListOf<FreeSlot>()
        var cursor = windowStart
        for ((busyStart, busyEnd) in busyPeriods) {
            if (cursor < busyStart) {
                val slot = FreeSlot(cursor, busyStart)
                if (slot.durationMinutes >= minSlotMinutes) freeSlots.add(slot)
            }
            if (busyEnd > cursor) cursor = busyEnd
        }
        if (cursor < windowEnd) {
            val slot = FreeSlot(cursor, windowEnd)
            if (slot.durationMinutes >= minSlotMinutes) freeSlots.add(slot)
        }
        return freeSlots
    }

    private fun mergePeriods(sorted: List<Pair<Instant, Instant>>): List<Pair<Instant, Instant>> {
        if (sorted.isEmpty()) return emptyList()
        val merged = mutableListOf(sorted[0])
        for ((start, end) in sorted.drop(1)) {
            val last = merged.last()
            if (start <= last.second) {
                merged[merged.lastIndex] = last.first to maxOf(last.second, end)
            } else {
                merged.add(start to end)
            }
        }
        return merged
    }
}
