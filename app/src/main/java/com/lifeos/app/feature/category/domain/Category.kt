package com.lifeos.app.feature.category.domain

import java.time.Instant

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#1B5E20",
    val icon: String = "star",
    val orderIndex: Int = 0,
    val isArchived: Boolean = false,
    val isHidden: Boolean = false,
    val createdAt: Instant = Instant.now(),
)
