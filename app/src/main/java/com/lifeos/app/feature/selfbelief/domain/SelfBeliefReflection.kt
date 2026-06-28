package com.lifeos.app.feature.selfbelief.domain

import java.time.Instant

data class SelfBeliefReflection(
    val id: String,
    val dateTime: Instant,
    val whatHappened: String = "",
    val storyTelling: String = "",
    val evidenceFor: String = "",
    val evidenceAgainst: String = "",
    val friendAdvice: String = "",
    val strengthsThatRemain: String = "",
    val nextSmallAction: String = "",
)
