package com.lifeos.app.feature.inspiration.domain

import java.time.Instant

enum class InspirationType { QUOTE, IMAGE }

data class InspirationItem(
    val id: String,
    val type: InspirationType,
    val text: String = "",
    val author: String = "",
    val imagePath: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now(),
)
