package com.lifeos.app.feature.journal.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class JournalDemoSeeder @Inject constructor(
    private val dao: JournalDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val entries = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            listOf(
                "Shipped the new onboarding flow" to "After two weeks of work, the new onboarding flow is live. Early metrics look promising.",
                "Tough sprint planning" to "We overcommitted again. Need to push back harder on scope next time.",
            )
        } else {
            listOf(
                "Started a new book" to "Picked up a novel I've been meaning to read for months. Already hooked.",
                "Good catch-up with an old friend" to "Hadn't talked in a year, picked up right where we left off.",
            )
        }
        entries.forEachIndexed { index, (title, body) ->
            val time = now.minus((index + 1).toLong(), ChronoUnit.DAYS)
            dao.upsert(
                JournalEntryEntity(
                    id = "${profileId}_journal_${index + 1}", dateTime = time, title = title, body = body,
                    tags = emptyList(), categoryId = null, moodEntryId = null, goalIds = emptyList(), habitIds = emptyList(),
                    peopleMentioned = emptyList(), location = null, weather = null, imageUris = emptyList(),
                    voiceNoteUris = emptyList(), createdAt = time, updatedAt = time, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
