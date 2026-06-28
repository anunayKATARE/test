package com.lifeos.app.feature.category.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.category.domain.Category
import java.time.Instant

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val colorHex: String,
    val icon: String,
    val orderIndex: Int,
    val isArchived: Boolean,
    val isHidden: Boolean,
    val createdAt: Instant,
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    icon = icon,
    orderIndex = orderIndex,
    isArchived = isArchived,
    isHidden = isHidden,
    createdAt = createdAt,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    icon = icon,
    orderIndex = orderIndex,
    isArchived = isArchived,
    isHidden = isHidden,
    createdAt = createdAt,
)
