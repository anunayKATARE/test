package com.lifeos.app.feature.mood.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.feature.mood.domain.Emotion
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MoodDemoSeeder @Inject constructor(
    private val dao: MoodDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val entries = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            listOf(
                Triple(Emotion.ANXIETY, "Tight deadline") to "Worried about the release date slipping.",
                Triple(Emotion.PRIDE, "Demo went well") to "Stakeholders loved the new feature demo.",
                Triple(Emotion.FRUSTRATION, "Flaky CI") to "Build failed three times for unrelated reasons.",
            )
        } else {
            listOf(
                Triple(Emotion.JOY, "Weekend hike") to "Felt great being outdoors with friends.",
                Triple(Emotion.GRATITUDE, "Family dinner") to "Appreciated time with family.",
                Triple(Emotion.CALM, "Quiet morning") to "Slow coffee and journaling before the day started.",
            )
        }
        entries.forEachIndexed { index, (data, lesson) ->
            val (emotion, trigger) = data
            dao.upsert(
                MoodEntryEntity(
                    id = "${profileId}_mood_${index + 1}", dateTime = now.minus((index + 1).toLong(), ChronoUnit.DAYS),
                    emotion = emotion, intensity = 6, trigger = trigger, peopleInvolved = emptyList(), situation = "",
                    automaticThoughts = "", physicalSensations = "", actionsTaken = "", recoveryTimeMinutes = null,
                    lessonsLearned = lesson, categoryId = null, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
