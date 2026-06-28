package com.lifeos.app.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeUtils {
    val zone: ZoneId = ZoneId.systemDefault()

    fun now(): Instant = Instant.now()

    fun today(): LocalDate = LocalDate.now(zone)

    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun daysBetween(start: LocalDate, end: LocalDate): Long = ChronoUnit.DAYS.between(start, end)

    fun weeksInRange(weeks: Int): List<LocalDate> {
        val today = today()
        return (0 until weeks * 7).map { offset -> today.minusDays(offset.toLong()) }.reversed()
    }

    fun formatDisplayDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    fun formatDisplayDateTime(instant: Instant): String =
        DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
            .withZone(zone)
            .format(instant)
}
