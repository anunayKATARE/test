package com.lifeos.app.feature.mentaltoughness.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject

class LogMentalToughnessEntryUseCase @Inject constructor(
    private val repository: MentalToughnessRepository,
) {
    suspend operator fun invoke(
        type: MentalToughnessType,
        title: String,
        description: String = "",
        emotionBefore: String = "",
        emotionAfter: String = "",
        outcome: String = "",
        lessonLearned: String = "",
        dateTime: Instant = Instant.now(),
    ): MentalToughnessEntry {
        val entry = MentalToughnessEntry(
            id = IdGenerator.newId(),
            type = type,
            dateTime = dateTime,
            title = title,
            description = description,
            emotionBefore = emotionBefore,
            emotionAfter = emotionAfter,
            outcome = outcome,
            lessonLearned = lessonLearned,
        )
        repository.upsert(entry)
        return entry
    }
}

class DeleteMentalToughnessEntryUseCase @Inject constructor(
    private val repository: MentalToughnessRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
