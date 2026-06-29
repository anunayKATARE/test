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
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    date = date,
    completed = completed,
    createdAt = createdAt,
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    date = date,
    completed = completed,
    createdAt = createdAt,
)
