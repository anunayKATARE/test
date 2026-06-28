package com.lifeos.app.ai

import javax.inject.Inject

/**
 * Default [AiInsightProvider] used until a real LLM-backed implementation is wired in. Keeps the
 * app fully functional without any AI dependency by reporting itself as unavailable everywhere.
 */
class NoOpAiInsightProvider @Inject constructor() : AiInsightProvider {

    override val isAvailable: Boolean = false

    private fun <T> unavailable(): AiResult<T> = AiResult.Unavailable("AI features are not configured on this device")

    override suspend fun summarizeJournalEntries(entriesText: List<String>): AiResult<String> = unavailable()

    override suspend fun generateWeeklyReviewNarrative(structuredSummary: String): AiResult<String> = unavailable()

    override suspend fun generateMonthlyReviewNarrative(structuredSummary: String): AiResult<String> = unavailable()

    override suspend fun detectPatterns(context: String): AiResult<List<String>> = unavailable()

    override suspend fun suggestHabits(context: String): AiResult<List<String>> = unavailable()

    override suspend fun suggestCopingStrategies(situation: String): AiResult<List<String>> = unavailable()

    override suspend fun answerQuestionAboutHistory(question: String, context: String): AiResult<String> = unavailable()

    override suspend fun generateMotivationalInsight(context: String): AiResult<String> = unavailable()
}
