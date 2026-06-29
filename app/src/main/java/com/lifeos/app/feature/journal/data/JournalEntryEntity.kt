package com.lifeos.app.feature.journal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.journal.domain.JournalEntry
import java.time.Instant

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val dateTime: Instant,
    val title: String,
    val body: String,
    val tags: List<String>,
    val categoryId: String?,
    val moodEntryId: String?,
    val goalIds: List<String>,
    val habitIds: List<String>,
    val peopleMentioned: List<String>,
    val location: String?,
    val weather: String?,
    val imageUris: List<String>,
    val voiceNoteUris: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val profileId: String? = null,
)

fun JournalEntryEntity.toDomain() = JournalEntry(
    id = id,
    dateTime = dateTime,
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
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun JournalEntry.toEntity() = JournalEntryEntity(
    id = id,
    dateTime = dateTime,
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
    createdAt = createdAt,
    updatedAt = updatedAt,
)
