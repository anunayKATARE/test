package com.lifeos.app.feature.journal.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject

class CreateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(
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
        dateTime: Instant = Instant.now(),
    ): JournalEntry {
        val entry = JournalEntry(
            id = IdGenerator.newId(),
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
        )
        repository.upsert(entry)
        return entry
    }
}

class UpdateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(entry: JournalEntry) {
        repository.upsert(entry.copy(updatedAt = Instant.now()))
    }
}

class DeleteJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.delete(id)
    }
}

class SearchJournalEntriesUseCase @Inject constructor(
    private val repository: JournalRepository,
) {
    suspend operator fun invoke(query: String): List<JournalEntry> = repository.search(query)
}
