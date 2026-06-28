package com.lifeos.app.feature.inspiration.domain

import com.lifeos.app.core.common.IdGenerator
import javax.inject.Inject

class AddQuoteUseCase @Inject constructor(
    private val repository: InspirationRepository,
) {
    suspend operator fun invoke(text: String, author: String = ""): InspirationItem {
        val item = InspirationItem(
            id = IdGenerator.newId(),
            type = InspirationType.QUOTE,
            text = text,
            author = author,
        )
        repository.upsert(item)
        return item
    }
}

class AddImageUseCase @Inject constructor(
    private val repository: InspirationRepository,
) {
    suspend operator fun invoke(imagePath: String): InspirationItem {
        val item = InspirationItem(
            id = IdGenerator.newId(),
            type = InspirationType.IMAGE,
            imagePath = imagePath,
        )
        repository.upsert(item)
        return item
    }
}

class DeleteInspirationItemUseCase @Inject constructor(
    private val repository: InspirationRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
