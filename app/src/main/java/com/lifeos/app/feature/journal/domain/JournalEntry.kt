package com.lifeos.app.feature.journal.domain

import java.time.Instant

data class JournalEntry(
    val id: String,
    val dateTime: Instant,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val categoryId: String? = null,
    val moodEntryId: String? = null,
    val goalIds: List<String> = emptyList(),
    val habitIds: List<String> = emptyList(),
    val peopleMentioned: List<String> = emptyList(),
    val location: String? = null,
    val weather: String? = null,
    val imageUris: List<String> = emptyList(),
    val voiceNoteUris: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
