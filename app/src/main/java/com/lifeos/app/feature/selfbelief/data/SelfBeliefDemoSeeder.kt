package com.lifeos.app.feature.selfbelief.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class SelfBeliefDemoSeeder @Inject constructor(
    private val dao: SelfBeliefDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val reflection = if (template == DemoTemplate.WORK) {
            SelfBeliefEntity(
                id = "${profileId}_sb_1", dateTime = anchor.minus(3, ChronoUnit.DAYS),
                whatHappened = "Felt like I didn't deserve my promotion.",
                storyTelling = "I'm not as skilled as the rest of the team.",
                evidenceFor = "I still have to look things up sometimes.",
                evidenceAgainst = "I shipped two major features this quarter and mentored a new hire.",
                friendAdvice = "Everyone looks things up. You earned this.",
                strengthsThatRemain = "Persistence and willingness to learn.",
                nextSmallAction = "Write down one win at the end of each day.",
                profileId = profileId,
            )
        } else {
            SelfBeliefEntity(
                id = "${profileId}_sb_1", dateTime = anchor.minus(3, ChronoUnit.DAYS),
                whatHappened = "Skipped a workout and felt like a failure.",
                storyTelling = "I never stick to anything.",
                evidenceFor = "I've missed a few sessions this month.",
                evidenceAgainst = "I've worked out 3x a week for two months straight overall.",
                friendAdvice = "One missed day doesn't undo your progress.",
                strengthsThatRemain = "Consistency over the long run.",
                nextSmallAction = "Go for a short walk tomorrow, no pressure.",
                profileId = profileId,
            )
        }
        dao.upsert(reflection)
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
