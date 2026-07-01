package com.lifeos.app.feature.category.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import java.time.Instant
import javax.inject.Inject

class CategoryDemoSeeder @Inject constructor(
    private val dao: CategoryDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val names = if (template == DemoTemplate.WORK) {
            listOf("Engineering" to "#1B5E20", "Product" to "#0D47A1", "Career Growth" to "#E65100")
        } else {
            listOf("Health" to "#1B5E20", "Relationships" to "#AD1457", "Finance" to "#F9A825")
        }
        names.forEachIndexed { index, (name, color) ->
            dao.upsert(
                CategoryEntity(
                    id = "${profileId}_category_${index + 1}", name = name, description = "",
                    colorHex = color, icon = "star", orderIndex = index, isArchived = false,
                    isHidden = false, createdAt = anchor, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
