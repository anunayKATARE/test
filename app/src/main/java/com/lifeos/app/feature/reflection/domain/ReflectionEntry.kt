package com.lifeos.app.feature.reflection.domain

import java.time.Instant

enum class ReflectionTemplateType {
    MORNING,
    EVENING,
    WEEKLY_REVIEW,
    MONTHLY_REVIEW,
    QUARTERLY_REVIEW,
    YEARLY_REVIEW,
    FAILURE_REVIEW,
    SUCCESS_REVIEW,
    RELATIONSHIP,
    CAREER,
    GRATITUDE,
    DECISION,
    LEARNING,
    BOOK_NOTES,
    MEETING,
    TRAVEL,
}

/** The ordered prompts shown for each reflection template, kept fully offline/static. */
object ReflectionTemplates {
    val prompts: Map<ReflectionTemplateType, List<String>> = mapOf(
        ReflectionTemplateType.MORNING to listOf(
            "What would make today great?",
            "What am I looking forward to?",
            "What is my top priority today?",
        ),
        ReflectionTemplateType.EVENING to listOf(
            "What went well today?",
            "What could have gone better?",
            "What am I grateful for tonight?",
        ),
        ReflectionTemplateType.WEEKLY_REVIEW to listOf(
            "What were my wins this week?",
            "What did I struggle with?",
            "What will I focus on next week?",
        ),
        ReflectionTemplateType.MONTHLY_REVIEW to listOf(
            "What progress did I make this month?",
            "What patterns am I noticing?",
            "What should change next month?",
        ),
        ReflectionTemplateType.QUARTERLY_REVIEW to listOf(
            "Did I move closer to my quarterly goals?",
            "What's working and what isn't?",
            "What are my priorities for next quarter?",
        ),
        ReflectionTemplateType.YEARLY_REVIEW to listOf(
            "What defined this year?",
            "What am I proudest of?",
            "Who do I want to become next year?",
        ),
        ReflectionTemplateType.FAILURE_REVIEW to listOf(
            "What happened?",
            "What was my role in it?",
            "What will I do differently next time?",
        ),
        ReflectionTemplateType.SUCCESS_REVIEW to listOf(
            "What worked?",
            "What can I repeat?",
            "Who or what helped me succeed?",
        ),
        ReflectionTemplateType.RELATIONSHIP to listOf(
            "Who is this about?",
            "What happened between us?",
            "What do I want to do next?",
        ),
        ReflectionTemplateType.CAREER to listOf(
            "Where am I in my career right now?",
            "What skill should I develop next?",
            "What's one career risk worth taking?",
        ),
        ReflectionTemplateType.GRATITUDE to listOf(
            "What are three things I'm grateful for today?",
            "Who made a positive difference recently?",
        ),
        ReflectionTemplateType.DECISION to listOf(
            "What decision am I facing?",
            "What are the options and trade-offs?",
            "What does my gut say?",
        ),
        ReflectionTemplateType.LEARNING to listOf(
            "What did I learn?",
            "How will I apply it?",
        ),
        ReflectionTemplateType.BOOK_NOTES to listOf(
            "What book/article is this about?",
            "Key ideas worth remembering",
            "How does this apply to my life?",
        ),
        ReflectionTemplateType.MEETING to listOf(
            "What was discussed?",
            "What are my action items?",
        ),
        ReflectionTemplateType.TRAVEL to listOf(
            "Where did I go and with whom?",
            "What moments stood out?",
            "What did this trip teach me?",
        ),
    )
}

data class ReflectionEntry(
    val id: String,
    val templateType: ReflectionTemplateType,
    val dateTime: Instant,
    val title: String = "",
    val answers: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
)
