package com.lifeos.app.feature.reflection.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ReflectionDemoSeeder @Inject constructor(
    private val dao: ReflectionDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val answers = if (template == DemoTemplate.WORK) {
            mapOf(
                "What were my wins this week?" to "Shipped the onboarding redesign.",
                "What did I struggle with?" to "Estimating sprint scope accurately.",
                "What will I focus on next week?" to "Pairing more with the new hire.",
            )
        } else {
            mapOf(
                "What went well today?" to "Got a good workout in and finished a chapter of my book.",
                "What could have gone better?" to "Spent too much time on my phone before bed.",
                "What am I grateful for tonight?" to "A good call with an old friend.",
            )
        }
        val type = if (template == DemoTemplate.WORK) ReflectionTemplateType.WEEKLY_REVIEW else ReflectionTemplateType.EVENING
        dao.upsert(
            ReflectionEntity(
                id = "${profileId}_reflection_1", templateType = type,
                dateTime = anchor.minus(1, ChronoUnit.DAYS),
                title = "", answers = answers, tags = emptyList(), profileId = profileId,
            ),
        )
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
