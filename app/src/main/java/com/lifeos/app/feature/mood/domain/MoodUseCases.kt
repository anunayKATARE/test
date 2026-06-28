package com.lifeos.app.feature.mood.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AddMoodUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(
        emotion: Emotion,
        intensity: Int,
        trigger: String = "",
        peopleInvolved: List<String> = emptyList(),
        situation: String = "",
        automaticThoughts: String = "",
        physicalSensations: String = "",
        actionsTaken: String = "",
        recoveryTimeMinutes: Int? = null,
        lessonsLearned: String = "",
        categoryId: String? = null,
        dateTime: Instant = Instant.now(),
    ): MoodEntry {
        val entry = MoodEntry(
            id = IdGenerator.newId(),
            dateTime = dateTime,
            emotion = emotion,
            intensity = intensity.coerceIn(1, 10),
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
        repository.upsert(entry)
        return entry
    }
}

class UpdateMoodUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(entry: MoodEntry) = repository.upsert(entry)
}

class DeleteMoodUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

class GetMoodHistoryUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(start: Instant, end: Instant): List<MoodEntry> =
        repository.observeBetween(start, end).first()
}

data class MoodPattern(val emotion: Emotion, val trigger: String, val occurrences: Int)

/**
 * Rule-based recurring pattern detector. Groups historical entries by (emotion, trigger) and
 * surfaces combinations that repeat, e.g. "anxious" + "presentation" three times. Works fully
 * offline; the AI layer can later add richer natural-language pattern descriptions on top.
 */
class DetectMoodPatternsUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(minOccurrences: Int = 2): List<MoodPattern> =
        detectFrom(repository.observeAll().first(), minOccurrences)

    fun detectFrom(entries: List<MoodEntry>, minOccurrences: Int = 2): List<MoodPattern> =
        entries
            .filter { it.trigger.isNotBlank() }
            .groupBy { it.emotion to it.trigger.trim().lowercase() }
            .map { (key, group) -> MoodPattern(key.first, key.second, group.size) }
            .filter { it.occurrences >= minOccurrences }
            .sortedByDescending { it.occurrences }
}
