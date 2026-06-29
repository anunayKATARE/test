package com.lifeos.app.feature.mood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.MoodEntry
import java.time.Instant

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey val id: String,
    val dateTime: Instant,
    val emotion: Emotion,
    val intensity: Int,
    val trigger: String,
    val peopleInvolved: List<String>,
    val situation: String,
    val automaticThoughts: String,
    val physicalSensations: String,
    val actionsTaken: String,
    val recoveryTimeMinutes: Int?,
    val lessonsLearned: String,
    val categoryId: String?,
    val profileId: String? = null,
)

fun MoodEntryEntity.toDomain() = MoodEntry(
    id = id,
    dateTime = dateTime,
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

fun MoodEntry.toEntity() = MoodEntryEntity(
    id = id,
    dateTime = dateTime,
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
