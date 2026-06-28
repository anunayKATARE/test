package com.lifeos.app.feature.selfbelief.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefReflection
import java.time.Instant

@Entity(tableName = "self_belief_reflections")
data class SelfBeliefEntity(
    @PrimaryKey val id: String,
    val dateTime: Instant,
    val whatHappened: String,
    val storyTelling: String,
    val evidenceFor: String,
    val evidenceAgainst: String,
    val friendAdvice: String,
    val strengthsThatRemain: String,
    val nextSmallAction: String,
)

fun SelfBeliefEntity.toDomain() = SelfBeliefReflection(
    id = id,
    dateTime = dateTime,
    whatHappened = whatHappened,
    storyTelling = storyTelling,
    evidenceFor = evidenceFor,
    evidenceAgainst = evidenceAgainst,
    friendAdvice = friendAdvice,
    strengthsThatRemain = strengthsThatRemain,
    nextSmallAction = nextSmallAction,
)

fun SelfBeliefReflection.toEntity() = SelfBeliefEntity(
    id = id,
    dateTime = dateTime,
    whatHappened = whatHappened,
    storyTelling = storyTelling,
    evidenceFor = evidenceFor,
    evidenceAgainst = evidenceAgainst,
    friendAdvice = friendAdvice,
    strengthsThatRemain = strengthsThatRemain,
    nextSmallAction = nextSmallAction,
)
