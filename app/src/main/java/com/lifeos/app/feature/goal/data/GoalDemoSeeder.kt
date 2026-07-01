package com.lifeos.app.feature.goal.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalStatus
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class GoalDemoSeeder @Inject constructor(
    private val dao: GoalDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val goals = if (template == DemoTemplate.WORK) {
            listOf(
                GoalEntity(
                    id = "${profileId}_goal_1", title = "Ship the Q3 roadmap", horizon = GoalHorizon.QUARTERLY,
                    description = "Deliver all three committed features before the quarter ends.",
                    targetDate = LocalDate.now().plusMonths(2), status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
                GoalEntity(
                    id = "${profileId}_goal_2", title = "Get AWS Solutions Architect certified",
                    horizon = GoalHorizon.MONTHLY,
                    description = "Finish the course and pass the exam.",
                    targetDate = LocalDate.now().plusMonths(1), status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
                GoalEntity(
                    id = "${profileId}_goal_3", title = "Mentor two junior engineers",
                    horizon = GoalHorizon.LONG_TERM,
                    description = "Run biweekly 1:1s and pair-programming sessions.",
                    targetDate = null, status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
            )
        } else {
            listOf(
                GoalEntity(
                    id = "${profileId}_goal_1", title = "Run a 10K", horizon = GoalHorizon.QUARTERLY,
                    description = "Build up from 5K over the next 3 months.",
                    targetDate = LocalDate.now().plusMonths(3), status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
                GoalEntity(
                    id = "${profileId}_goal_2", title = "Read 12 books this year", horizon = GoalHorizon.LONG_TERM,
                    description = "One book a month, mix of fiction and non-fiction.",
                    targetDate = null, status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
                GoalEntity(
                    id = "${profileId}_goal_3", title = "Save \$5,000 emergency fund", horizon = GoalHorizon.MONTHLY,
                    description = "Automate a transfer every payday.",
                    targetDate = LocalDate.now().plusMonths(4), status = GoalStatus.ACTIVE,
                    linkedHabitIds = emptyList(), linkedJournalIds = emptyList(),
                    categoryId = null, createdAt = anchor, completedAt = null, profileId = profileId,
                ),
            )
        }
        goals.forEach { dao.upsert(it) }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
