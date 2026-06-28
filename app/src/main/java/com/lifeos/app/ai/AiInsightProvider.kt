package com.lifeos.app.ai

/**
 * Abstraction for optional AI-powered enhancements. The app must be fully usable with the
 * [NoOpAiInsightProvider] default; concrete LLM-backed implementations (e.g. an on-device model
 * or a remote API client) can be swapped in via Hilt without touching any feature code.
 */
interface AiInsightProvider {
    val isAvailable: Boolean

    suspend fun summarizeJournalEntries(entriesText: List<String>): AiResult<String>
    suspend fun generateWeeklyReviewNarrative(structuredSummary: String): AiResult<String>
    suspend fun generateMonthlyReviewNarrative(structuredSummary: String): AiResult<String>
    suspend fun detectPatterns(context: String): AiResult<List<String>>
    suspend fun suggestHabits(context: String): AiResult<List<String>>
    suspend fun suggestCopingStrategies(situation: String): AiResult<List<String>>
    suspend fun answerQuestionAboutHistory(question: String, context: String): AiResult<String>
    suspend fun generateMotivationalInsight(context: String): AiResult<String>
}

sealed interface AiResult<out T> {
    data class Success<T>(val value: T) : AiResult<T>
    data class Unavailable(val reason: String) : AiResult<Nothing>
    data class Error(val message: String) : AiResult<Nothing>
}
