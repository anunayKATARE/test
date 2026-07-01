package com.lifeos.app.feature.habit.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AddHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
        customDaysOfWeek: List<Int> = emptyList(),
        difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
        importance: HabitImportance = HabitImportance.MEDIUM,
        categoryId: String? = null,
        triggers: List<String> = emptyList(),
    ): Habit {
        val habit = Habit(
            id = IdGenerator.newId(),
            title = title,
            description = description,
            scheduleType = scheduleType,
            customDaysOfWeek = customDaysOfWeek,
            difficulty = difficulty,
            importance = importance,
            categoryId = categoryId,
            triggers = triggers,
        )
        repository.upsertHabit(habit)
        return habit
    }
}

class EditHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit) = repository.upsertHabit(habit)
}

class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteHabit(id)
}

class ArchiveHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(id: String, archived: Boolean = true) = repository.setArchived(id, archived)
}

/** Marks a habit done (or not done) for a given day. Defaults to today — this is the function the
 * dashboard's quick-complete action and the AppFunctions facade both call. */
class CompleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: String, date: LocalDate = LocalDate.now(), note: String = "") {
        repository.setCompletion(habitId, date, completed = true, note = note)
    }
}

class UncompleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: String, date: LocalDate = LocalDate.now()) {
        repository.setCompletion(habitId, date, completed = false)
    }
}

/**
 * Computes streaks and completion rate for a habit, honouring its schedule type:
 * - DAILY: every calendar day is an expected occurrence.
 * - CUSTOM: only the configured ISO days-of-week (1=Mon..7=Sun) are expected.
 * - WEEKLY: at least one completion expected per calendar week.
 * - MONTHLY: at least one completion expected per calendar month.
 */
class CalculateHabitStatsUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit, lookbackDays: Int = 90): HabitStats {
        val today = LocalDate.now()
        val windowStart = today.minusDays(lookbackDays.toLong())
        val createdDate = habit.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val start = if (createdDate.isAfter(windowStart)) createdDate else windowStart
        val completions = repository.getCompletionsBetween(habit.id, start, today)
        val completedDates = completions.filter { it.completed }.map { it.date }.toSet()

        return when (habit.scheduleType) {
            HabitScheduleType.DAILY -> dailyStats(start, today, completedDates) { true }
            HabitScheduleType.CUSTOM -> dailyStats(start, today, completedDates) { date ->
                habit.customDaysOfWeek.contains(date.dayOfWeek.value)
            }
            HabitScheduleType.WEEKLY -> periodStats(start, today, completedDates) { it.weekFields() }
            HabitScheduleType.MONTHLY -> periodStats(start, today, completedDates) { "${it.year}-${it.monthValue}" }
        }
    }

    private fun LocalDate.weekFields(): String {
        val week = java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()
        return "${get(java.time.temporal.WeekFields.ISO.weekBasedYear())}-${get(week)}"
    }

    private fun dailyStats(
        start: LocalDate,
        today: LocalDate,
        completedDates: Set<LocalDate>,
        isExpected: (LocalDate) -> Boolean,
    ): HabitStats {
        var expectedCount = 0
        var completedCount = 0
        var date = start
        while (!date.isAfter(today)) {
            if (isExpected(date)) {
                expectedCount++
                if (completedDates.contains(date)) completedCount++
            }
            date = date.plusDays(1)
        }

        var currentStreak = 0
        var probe = today
        var streakBroken = false
        while (!probe.isBefore(start) && !streakBroken) {
            if (isExpected(probe)) {
                if (completedDates.contains(probe)) currentStreak++ else streakBroken = true
            }
            probe = probe.minusDays(1)
        }

        var longestStreak = 0
        var running = 0
        date = start
        while (!date.isAfter(today)) {
            if (isExpected(date)) {
                running = if (completedDates.contains(date)) running + 1 else 0
                if (running > longestStreak) longestStreak = running
            }
            date = date.plusDays(1)
        }

        val rate = if (expectedCount == 0) 0f else completedCount.toFloat() / expectedCount
        return HabitStats(currentStreak, longestStreak, rate)
    }

    private fun periodStats(
        start: LocalDate,
        today: LocalDate,
        completedDates: Set<LocalDate>,
        periodKey: (LocalDate) -> String,
    ): HabitStats {
        val periods = linkedMapOf<String, Boolean>()
        var date = start
        while (!date.isAfter(today)) {
            val key = periodKey(date)
            val hasCompletion = completedDates.contains(date)
            periods[key] = (periods[key] ?: false) || hasCompletion
            date = date.plusDays(1)
        }
        val ordered = periods.entries.toList()
        val expectedCount = ordered.size
        val completedCount = ordered.count { it.value }

        var currentStreak = 0
        for (i in ordered.indices.reversed()) {
            if (ordered[i].value) currentStreak++ else break
        }

        var longestStreak = 0
        var running = 0
        for (entry in ordered) {
            running = if (entry.value) running + 1 else 0
            if (running > longestStreak) longestStreak = running
        }

        val rate = if (expectedCount == 0) 0f else completedCount.toFloat() / expectedCount
        return HabitStats(currentStreak, longestStreak, rate)
    }
}

class GetTodaysHabitsUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(): List<Habit> {
        val today = LocalDate.now()
        return repository.observeActiveHabits().first().filter { it.isScheduledOn(today) }
    }
}
