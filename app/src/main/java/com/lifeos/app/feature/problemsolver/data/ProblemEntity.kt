package com.lifeos.app.feature.problemsolver.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import java.time.Instant

@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val possibleCauses: List<String>,
    val attemptsMade: List<String>,
    val whatWorked: String,
    val whatFailed: String,
    val status: ProblemStatus,
    val notes: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val profileId: String? = null,
)

fun ProblemEntity.toDomain() = Problem(
    id = id,
    title = title,
    description = description,
    possibleCauses = possibleCauses,
    attemptsMade = attemptsMade,
    whatWorked = whatWorked,
    whatFailed = whatFailed,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Problem.toEntity() = ProblemEntity(
    id = id,
    title = title,
    description = description,
    possibleCauses = possibleCauses,
    attemptsMade = attemptsMade,
    whatWorked = whatWorked,
    whatFailed = whatFailed,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
