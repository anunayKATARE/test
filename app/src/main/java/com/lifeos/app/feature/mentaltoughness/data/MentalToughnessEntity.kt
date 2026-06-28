package com.lifeos.app.feature.mentaltoughness.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessEntry
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import java.time.Instant

@Entity(tableName = "mental_toughness_entries")
data class MentalToughnessEntity(
    @PrimaryKey val id: String,
    val type: MentalToughnessType,
    val dateTime: Instant,
    val title: String,
    val description: String,
    val emotionBefore: String,
    val emotionAfter: String,
    val outcome: String,
    val lessonLearned: String,
)

fun MentalToughnessEntity.toDomain() = MentalToughnessEntry(
    id = id,
    type = type,
    dateTime = dateTime,
    title = title,
    description = description,
    emotionBefore = emotionBefore,
    emotionAfter = emotionAfter,
    outcome = outcome,
    lessonLearned = lessonLearned,
)

fun MentalToughnessEntry.toEntity() = MentalToughnessEntity(
    id = id,
    type = type,
    dateTime = dateTime,
    title = title,
    description = description,
    emotionBefore = emotionBefore,
    emotionAfter = emotionAfter,
    outcome = outcome,
    lessonLearned = lessonLearned,
)
