package com.lifeos.app.feature.checkin.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.lifeos.app.feature.checkin.domain.CheckInCommitment
import com.lifeos.app.feature.checkin.domain.CheckInSession
import com.lifeos.app.feature.checkin.domain.CommitmentType
import java.time.Instant

@Entity(tableName = "check_in_sessions")
data class CheckInSessionEntity(
    @PrimaryKey val id: String,
    val scheduledAt: Long,
    val completedAt: Long? = null,
    val profileId: String? = null,
)

@Entity(tableName = "check_in_commitments")
data class CheckInCommitmentEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val text: String,
    val linkedId: String? = null,
    val linkedType: String? = null,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
)

data class CheckInSessionWithCommitments(
    @Embedded val session: CheckInSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val commitments: List<CheckInCommitmentEntity>,
)

fun CheckInSessionWithCommitments.toDomain() = CheckInSession(
    id = session.id,
    scheduledAt = Instant.ofEpochMilli(session.scheduledAt),
    commitments = commitments.sortedBy { it.sortOrder }.map { it.toDomain() },
    completedAt = session.completedAt?.let { Instant.ofEpochMilli(it) },
)

fun CheckInCommitmentEntity.toDomain() = CheckInCommitment(
    id = id,
    text = text,
    linkedId = linkedId,
    linkedType = linkedType?.let { CommitmentType.valueOf(it) },
    isCompleted = isCompleted,
)

fun CheckInSession.toSessionEntity(profileId: String?) = CheckInSessionEntity(
    id = id,
    scheduledAt = scheduledAt.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    profileId = profileId,
)

fun CheckInCommitment.toEntity(sessionId: String, sortOrder: Int) = CheckInCommitmentEntity(
    id = id,
    sessionId = sessionId,
    text = text,
    linkedId = linkedId,
    linkedType = linkedType?.name,
    isCompleted = isCompleted,
    sortOrder = sortOrder,
)
