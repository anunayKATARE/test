package com.lifeos.app.feature.timelog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.timelog.domain.TimeLog
import java.time.Instant

@Entity(tableName = "time_logs")
data class TimeLogEntity(
    @PrimaryKey val id: String,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val linkedTaskId: String? = null,
    val chore: String? = null,
    val profileId: String? = null,
)

fun TimeLogEntity.toDomain() = TimeLog(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    linkedTaskId = linkedTaskId,
    chore = chore,
    profileId = profileId,
)

fun TimeLog.toEntity() = TimeLogEntity(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    linkedTaskId = linkedTaskId,
    chore = chore,
    profileId = profileId,
)
