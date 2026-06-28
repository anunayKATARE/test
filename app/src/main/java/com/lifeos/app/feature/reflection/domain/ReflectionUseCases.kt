package com.lifeos.app.feature.reflection.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject

class SaveReflectionEntryUseCase @Inject constructor(
    private val repository: ReflectionRepository,
) {
    suspend operator fun invoke(
        templateType: ReflectionTemplateType,
        title: String,
        answers: Map<String, String>,
        tags: List<String> = emptyList(),
        dateTime: Instant = Instant.now(),
        existingId: String? = null,
    ): ReflectionEntry {
        val entry = ReflectionEntry(
            id = existingId ?: IdGenerator.newId(),
            templateType = templateType,
            dateTime = dateTime,
            title = title,
            answers = answers,
            tags = tags,
        )
        repository.upsert(entry)
        return entry
    }
}

class DeleteReflectionEntryUseCase @Inject constructor(
    private val repository: ReflectionRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
