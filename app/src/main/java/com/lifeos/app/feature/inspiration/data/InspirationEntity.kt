package com.lifeos.app.feature.inspiration.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.inspiration.domain.InspirationItem
import com.lifeos.app.feature.inspiration.domain.InspirationType
import java.time.Instant

@Entity(tableName = "inspiration_items")
data class InspirationEntity(
    @PrimaryKey val id: String,
    val type: InspirationType,
    val text: String,
    val author: String,
    val imagePath: String?,
    val sortOrder: Int,
    val createdAt: Instant,
    val profileId: String? = null,
)

fun InspirationEntity.toDomain() = InspirationItem(
    id = id,
    type = type,
    text = text,
    author = author,
    imagePath = imagePath,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun InspirationItem.toEntity() = InspirationEntity(
    id = id,
    type = type,
    text = text,
    author = author,
    imagePath = imagePath,
    sortOrder = sortOrder,
    createdAt = createdAt,
)
