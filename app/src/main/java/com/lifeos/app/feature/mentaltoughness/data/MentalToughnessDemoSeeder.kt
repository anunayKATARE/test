package com.lifeos.app.feature.mentaltoughness.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MentalToughnessDemoSeeder @Inject constructor(
    private val dao: MentalToughnessDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val entry = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            MentalToughnessType.DIFFICULT_CONVERSATION to Triple(
                "Pushed back on an unrealistic deadline",
                "Nervous, worried about seeming difficult",
                "Relieved, more respected",
            )
        } else {
            MentalToughnessType.COURAGE_TRACKER to Triple(
                "Gave a toast at a friend's wedding despite my fear of public speaking",
                "Terrified",
                "Proud, surprised it went well",
            )
        }
        val (type, details) = entry
        val (title, before, after) = details
        dao.upsert(
            MentalToughnessEntity(
                id = "${profileId}_mt_1", type = type, dateTime = now.minus(2, ChronoUnit.DAYS), title = title,
                description = "", emotionBefore = before, emotionAfter = after, outcome = "", lessonLearned = "",
                profileId = profileId,
            ),
        )
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
