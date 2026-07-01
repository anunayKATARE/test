package com.lifeos.app.feature.task.data

import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.core.demo.DemoTemplate
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class TaskDemoSeeder @Inject constructor(
    private val dao: TaskDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant) {
        val today = LocalDate.now()
        val titles = if (template == DemoTemplate.WORK) {
            listOf("Review PR #482", "Prep sprint demo", "1:1 with manager")
        } else {
            listOf("Buy groceries", "Call mom", "Gym session")
        }
        titles.forEachIndexed { index, title ->
            dao.upsertTask(
                TaskEntity(
                    id = "${profileId}_task_${index + 1}", title = title, description = "",
                    date = today, completed = index == 0, createdAt = anchor, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
