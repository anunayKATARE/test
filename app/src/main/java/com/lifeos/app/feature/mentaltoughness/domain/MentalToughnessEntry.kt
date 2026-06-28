package com.lifeos.app.feature.mentaltoughness.domain

import java.time.Instant

enum class MentalToughnessType {
    DISCOMFORT_CHALLENGE,
    FEAR_LOG,
    FAILURE_JOURNAL,
    COURAGE_TRACKER,
    DIFFICULT_CONVERSATION,
    AVOIDED_THING,
    SETBACK_RECOVERY,
}

data class MentalToughnessEntry(
    val id: String,
    val type: MentalToughnessType,
    val dateTime: Instant,
    val title: String,
    val description: String = "",
    val emotionBefore: String = "",
    val emotionAfter: String = "",
    val outcome: String = "",
    val lessonLearned: String = "",
)
