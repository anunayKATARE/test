package com.lifeos.app.feature.task.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.task.domain.Task
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val date: LocalDate,
    val completed: Boolean,
    val createdAt: Instant,
    val profileId: String? = null,
    val triggers: List<String> = emptyList(),
    val scheduledAt: Long? = null,
    val scheduledEndAt: Long? = null,
    val isChore: Boolean = false,
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    date = date,
    completed = completed,
    createdAt = createdAt,
    triggers = triggers,
    scheduledAt = scheduledAt?.let { Instant.ofEpochMilli(it) },
    scheduledEndAt = scheduledEndAt?.let { Instant.ofEpochMilli(it) },
    isChore = isChore,
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    date = date,
    completed = completed,
    createdAt = createdAt,
    triggers = triggers,
    scheduledAt = scheduledAt?.toEpochMilli(),
    scheduledEndAt = scheduledEndAt?.toEpochMilli(),
    isChore = isChore,
)
