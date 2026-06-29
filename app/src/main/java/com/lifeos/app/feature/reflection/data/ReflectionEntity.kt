package com.lifeos.app.feature.reflection.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.reflection.domain.ReflectionEntry
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import java.time.Instant

@Entity(tableName = "reflection_entries")
data class ReflectionEntity(
    @PrimaryKey val id: String,
    val templateType: ReflectionTemplateType,
    val dateTime: Instant,
    val title: String,
    val answers: Map<String, String>,
    val tags: List<String>,
    val profileId: String? = null,
)

fun ReflectionEntity.toDomain() = ReflectionEntry(
    id = id,
    templateType = templateType,
    dateTime = dateTime,
    title = title,
    answers = answers,
    tags = tags,
)

fun ReflectionEntry.toEntity() = ReflectionEntity(
    id = id,
    templateType = templateType,
    dateTime = dateTime,
    title = title,
    answers = answers,
    tags = tags,
)
