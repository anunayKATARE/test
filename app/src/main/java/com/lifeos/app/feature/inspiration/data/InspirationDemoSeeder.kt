package com.lifeos.app.feature.inspiration.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.feature.inspiration.domain.InspirationType
import java.time.Instant
import javax.inject.Inject

class InspirationDemoSeeder @Inject constructor(
    private val dao: InspirationDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val quotes = if (template == DemoTemplate.WORK) {
            listOf(
                "Done is better than perfect." to "Sheryl Sandberg",
                "The best way to predict the future is to create it." to "Peter Drucker",
            )
        } else {
            listOf(
                "Small steps every day." to "Unknown",
                "Discipline is choosing between what you want now and what you want most." to "Abraham Lincoln",
            )
        }
        quotes.forEachIndexed { index, (text, author) ->
            dao.upsert(
                InspirationEntity(
                    id = "${profileId}_inspiration_${index + 1}", type = InspirationType.QUOTE, text = text,
                    author = author, imagePath = null, sortOrder = index, createdAt = anchor, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
