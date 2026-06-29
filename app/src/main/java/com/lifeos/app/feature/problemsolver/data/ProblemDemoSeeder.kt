package com.lifeos.app.feature.problemsolver.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import java.time.Instant
import javax.inject.Inject

class ProblemDemoSeeder @Inject constructor(
    private val dao: ProblemDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val problems = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            listOf("CI pipeline is flaky" to "Tests fail intermittently, slowing down every PR.")
        } else {
            listOf("Can't stick to a sleep schedule" to "I keep staying up late even when I plan to sleep early.")
        }
        problems.forEachIndexed { index, (title, description) ->
            dao.upsert(
                ProblemEntity(
                    id = "${profileId}_problem_${index + 1}", title = title, description = description,
                    possibleCauses = emptyList(), attemptsMade = emptyList(), whatWorked = "", whatFailed = "",
                    status = ProblemStatus.OPEN, notes = "", createdAt = now, updatedAt = now, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
