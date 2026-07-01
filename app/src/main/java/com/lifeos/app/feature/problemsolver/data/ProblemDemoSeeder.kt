package com.lifeos.app.feature.problemsolver.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import java.time.Instant
import javax.inject.Inject

class ProblemDemoSeeder @Inject constructor(
    private val dao: ProblemDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val problems = if (template == DemoTemplate.WORK) {
            listOf("CI pipeline is flaky" to "Tests fail intermittently, slowing down every PR.")
        } else {
            listOf("Can't stick to a sleep schedule" to "I keep staying up late even when I plan to sleep early.")
        }
        problems.forEachIndexed { index, (title, description) ->
            dao.upsert(
                ProblemEntity(
                    id = "${profileId}_problem_${index + 1}", title = title, description = description,
                    possibleCauses = emptyList(), attemptsMade = emptyList(), whatWorked = "",
                    whatFailed = "", status = ProblemStatus.OPEN, notes = "",
                    createdAt = anchor, updatedAt = anchor, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
