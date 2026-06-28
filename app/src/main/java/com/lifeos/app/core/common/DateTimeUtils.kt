package com.lifeos.app.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

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

    fun formatMonthTitle(month: YearMonth): String =
        "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"

    /**
     * Six full weeks (Monday-first) covering [month], padding with leading/trailing dates from the
     * adjacent months so every row has 7 days — the grid a calendar UI renders directly.
     */
    fun monthGrid(month: YearMonth): List<LocalDate> {
        val firstOfMonth = month.atDay(1)
        val leadingDays = (firstOfMonth.dayOfWeek.value - 1).coerceAtLeast(0)
        val gridStart = firstOfMonth.minusDays(leadingDays.toLong())
        return (0 until 42).map { offset -> gridStart.plusDays(offset.toLong()) }
    }
}
