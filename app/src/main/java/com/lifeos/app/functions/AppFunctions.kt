package com.lifeos.app.functions

import com.lifeos.app.ai.AiInsightProvider
import com.lifeos.app.ai.AiResult
import com.lifeos.app.feature.category.domain.HideCategoryUseCase
import com.lifeos.app.feature.goal.domain.AddGoalUseCase
import com.lifeos.app.feature.goal.domain.ArchiveGoalUseCase
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.goal.domain.GoalStatus
import com.lifeos.app.feature.goal.domain.GetTodaysGoalsUseCase
import com.lifeos.app.feature.habit.domain.CompleteHabitUseCase
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.journal.domain.CreateJournalEntryUseCase
import com.lifeos.app.feature.journal.domain.DeleteJournalEntryUseCase
import com.lifeos.app.feature.journal.domain.JournalEntry
import com.lifeos.app.feature.journal.domain.SearchJournalEntriesUseCase
import com.lifeos.app.feature.journal.domain.UpdateJournalEntryUseCase
import com.lifeos.app.feature.mood.domain.AddMoodUseCase
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.GetMoodHistoryUseCase
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.problemsolver.domain.GetOpenProblemsUseCase
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.RegisterProblemUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class SearchResults(
    val journalEntries: List<JournalEntry>,
    val goals: List<Goal>,
    val habits: List<Habit>,
    val problems: List<Problem>,
)

/**
 * Thin facade exposing LifeOS's core capabilities as small, composable, named functions. This is
 * the single surface a future assistant/LLM integration (or any other caller outside the
 * Compose/ViewModel layer) should depend on, rather than reaching into individual use cases
 * directly — keeping that integration point stable as feature internals evolve.
 */
class AppFunctions @Inject constructor(
    private val createJournalEntry: CreateJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val searchJournalEntries: SearchJournalEntriesUseCase,
    private val addMood: AddMoodUseCase,
    private val getMoodHistory: GetMoodHistoryUseCase,
    private val addGoal: AddGoalUseCase,
    private val archiveGoal: ArchiveGoalUseCase,
    private val getTodaysGoals: GetTodaysGoalsUseCase,
    private val completeHabit: CompleteHabitUseCase,
    private val hideCategory: HideCategoryUseCase,
    private val registerProblem: RegisterProblemUseCase,
    private val getOpenProblems: GetOpenProblemsUseCase,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val aiInsightProvider: AiInsightProvider,
) {

    suspend fun createJournalEntry(
        title: String,
        body: String,
        tags: List<String> = emptyList(),
        categoryId: String? = null,
        moodEntryId: String? = null,
        goalIds: List<String> = emptyList(),
        habitIds: List<String> = emptyList(),
        peopleMentioned: List<String> = emptyList(),
        location: String? = null,
        weather: String? = null,
        imageUris: List<String> = emptyList(),
        voiceNoteUris: List<String> = emptyList(),
    ): JournalEntry = createJournalEntry.invoke(
        title = title,
        body = body,
        tags = tags,
        categoryId = categoryId,
        moodEntryId = moodEntryId,
        goalIds = goalIds,
        habitIds = habitIds,
        peopleMentioned = peopleMentioned,
        location = location,
        weather = weather,
        imageUris = imageUris,
        voiceNoteUris = voiceNoteUris,
    )

    suspend fun updateJournalEntry(entry: JournalEntry) = updateJournalEntryUseCase.invoke(entry)

    suspend fun deleteJournalEntry(id: String) = deleteJournalEntryUseCase.invoke(id)

    suspend fun searchEntries(query: String): SearchResults {
        val journalMatches = searchJournalEntries.invoke(query)
        val needle = query.trim().lowercase()
        val goalMatches = if (needle.isEmpty()) emptyList() else
            goalRepository.observeAllGoals().first().filter { it.title.lowercase().contains(needle) }
        val habitMatches = if (needle.isEmpty()) emptyList() else
            habitRepository.observeAllHabits().first().filter { it.title.lowercase().contains(needle) }
        val problemMatches = if (needle.isEmpty()) emptyList() else
            getOpenProblems.invoke().filter { it.title.lowercase().contains(needle) }
        return SearchResults(journalMatches, goalMatches, habitMatches, problemMatches)
    }

    suspend fun addMood(
        emotion: Emotion,
        intensity: Int,
        trigger: String = "",
        peopleInvolved: List<String> = emptyList(),
        situation: String = "",
        automaticThoughts: String = "",
        physicalSensations: String = "",
        actionsTaken: String = "",
        recoveryTimeMinutes: Int? = null,
        lessonsLearned: String = "",
        categoryId: String? = null,
    ): MoodEntry = addMood.invoke(
        emotion = emotion,
        intensity = intensity,
        trigger = trigger,
        peopleInvolved = peopleInvolved,
        situation = situation,
        automaticThoughts = automaticThoughts,
        physicalSensations = physicalSensations,
        actionsTaken = actionsTaken,
        recoveryTimeMinutes = recoveryTimeMinutes,
        lessonsLearned = lessonsLearned,
        categoryId = categoryId,
    )

    suspend fun getMoodHistory(start: Instant, end: Instant): List<MoodEntry> = getMoodHistory.invoke(start, end)

    suspend fun addGoal(
        title: String,
        description: String = "",
        horizon: GoalHorizon = GoalHorizon.WEEKLY,
        targetDate: LocalDate? = null,
        linkedHabitIds: List<String> = emptyList(),
        linkedJournalIds: List<String> = emptyList(),
        categoryId: String? = null,
    ): Goal = addGoal.invoke(
        title = title,
        description = description,
        horizon = horizon,
        targetDate = targetDate,
        linkedHabitIds = linkedHabitIds,
        linkedJournalIds = linkedJournalIds,
        categoryId = categoryId,
    )

    suspend fun archiveGoal(id: String) = archiveGoal.invoke(id)

    suspend fun getTodaysGoals(): List<Goal> = getTodaysGoals.invoke()

    suspend fun completeHabit(habitId: String, date: LocalDate = LocalDate.now(), note: String = "") =
        completeHabit.invoke(habitId, date, note)

    suspend fun hideCategory(categoryId: String, hidden: Boolean = true) = hideCategory.invoke(categoryId, hidden)

    suspend fun registerProblem(
        title: String,
        description: String = "",
        possibleCauses: List<String> = emptyList(),
    ): Problem = registerProblem.invoke(title, description, possibleCauses)

    suspend fun getOpenProblems(): List<Problem> = getOpenProblems.invoke()

    suspend fun generateWeeklyReview(weekStart: LocalDate): String {
        val weekEnd = weekStart.plusDays(6)
        val summary = buildStructuredSummary(weekStart, weekEnd, GoalHorizon.WEEKLY)
        return when (val result = aiInsightProvider.generateWeeklyReviewNarrative(summary)) {
            is AiResult.Success -> result.value
            else -> summary
        }
    }

    suspend fun generateMonthlyReview(monthStart: LocalDate): String {
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val summary = buildStructuredSummary(monthStart, monthEnd, GoalHorizon.MONTHLY)
        return when (val result = aiInsightProvider.generateMonthlyReviewNarrative(summary)) {
            is AiResult.Success -> result.value
            else -> summary
        }
    }

    private suspend fun buildStructuredSummary(start: LocalDate, end: LocalDate, horizon: GoalHorizon): String {
        val zone = ZoneId.systemDefault()
        val startInstant = start.atStartOfDay(zone).toInstant()
        val endInstant = end.plusDays(1).atStartOfDay(zone).toInstant()

        val moods = getMoodHistory.invoke(startInstant, endInstant)
        val completions = habitRepository.completionCountByDay(start, end)
        val totalCompletions = completions.values.sum()
        val goals = goalRepository.observeGoalsByHorizon(horizon).first()
        val completedGoals = goals.count { it.status == GoalStatus.COMPLETED }

        val moodBreakdown = moods.groupingBy { it.emotion }.eachCount()
            .entries.joinToString(", ") { "${it.key.name.lowercase()}: ${it.value}" }
            .ifEmpty { "no mood entries logged" }

        return buildString {
            appendLine("Period: $start to $end")
            appendLine("Mood entries logged: ${moods.size} ($moodBreakdown)")
            appendLine("Habit completions: $totalCompletions")
            appendLine("Goals (${horizon.name.lowercase()}): ${goals.size} total, $completedGoals completed")
        }.trim()
    }
}
