package com.lifeos.app.feature.habit.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class HabitDemoSeeder @Inject constructor(
    private val dao: HabitDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val titles = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            listOf("Deep work block", "Inbox zero", "Daily standup notes")
        } else {
            listOf("Morning meditation", "Drink 8 glasses of water", "Evening walk")
        }
        titles.forEachIndexed { index, title ->
            val habitId = "${profileId}_habit_${index + 1}"
            dao.upsertHabit(
                HabitEntity(
                    id = habitId, title = title, description = "", scheduleType = HabitScheduleType.DAILY,
                    customDaysOfWeek = emptyList(), difficulty = HabitDifficulty.EASY, importance = HabitImportance.MEDIUM,
                    categoryId = null, isArchived = false, createdAt = now, profileId = profileId,
                ),
            )
            (0..6).forEach { daysAgo ->
                val date = LocalDate.now().minusDays(daysAgo.toLong())
                val completed = (daysAgo + index) % 3 != 0
                dao.upsertCompletion(
                    HabitCompletionEntity(
                        id = "${habitId}_$date", habitId = habitId, date = date, completed = completed, note = "",
                        profileId = profileId,
                    ),
                )
            }
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteCompletionsByProfile(profileId)
        dao.deleteHabitsByProfile(profileId)
    }
}
