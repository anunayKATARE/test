package com.lifeos.app.feature.mood.domain

import java.time.Instant

enum class Emotion {
    JOY, CALM, SADNESS, ANGER, ANXIETY, FEAR, SHAME, PRIDE, GRATITUDE, FRUSTRATION, LONELINESS, CONFIDENCE, NEUTRAL,
}

data class MoodEntry(
    val id: String,
    val dateTime: Instant,
    val emotion: Emotion,
    val intensity: Int,
    val trigger: String = "",
    val peopleInvolved: List<String> = emptyList(),
    val situation: String = "",
    val automaticThoughts: String = "",
    val physicalSensations: String = "",
    val actionsTaken: String = "",
    val recoveryTimeMinutes: Int? = null,
    val lessonsLearned: String = "",
    val categoryId: String? = null,
)
