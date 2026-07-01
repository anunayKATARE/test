package com.lifeos.app.feature.mood.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.feature.mood.domain.Emotion
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MoodDemoSeeder @Inject constructor(
    private val dao: MoodDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val entries = if (template == DemoTemplate.WORK) {
            listOf(
                (Emotion.ANXIETY to "Tight deadline") to "Worried about the release date slipping.",
                (Emotion.PRIDE to "Demo went well") to "Stakeholders loved the new feature demo.",
                (Emotion.FRUSTRATION to "Flaky CI") to "Build failed three times for unrelated reasons.",
            )
        } else {
            listOf(
                (Emotion.JOY to "Weekend hike") to "Felt great being outdoors with friends.",
                (Emotion.GRATITUDE to "Family dinner") to "Appreciated time with family.",
                (Emotion.CALM to "Quiet morning") to "Slow coffee and journaling before the day started.",
            )
        }
        entries.forEachIndexed { index, (data, lesson) ->
            val (emotion, trigger) = data
            dao.upsert(
                MoodEntryEntity(
                    id = "${profileId}_mood_${index + 1}",
                    dateTime = anchor.minus((index + 1).toLong(), ChronoUnit.DAYS),
                    emotion = emotion, intensity = 6, trigger = trigger, peopleInvolved = emptyList(),
                    situation = "", automaticThoughts = "", physicalSensations = "", actionsTaken = "",
                    recoveryTimeMinutes = null, lessonsLearned = lesson, categoryId = null, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
