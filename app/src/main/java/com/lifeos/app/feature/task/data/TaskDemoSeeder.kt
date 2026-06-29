package com.lifeos.app.feature.task.data

import com.lifeos.app.core.demo.DemoProfile
import com.lifeos.app.core.demo.DemoSeeder
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class TaskDemoSeeder @Inject constructor(
    private val dao: TaskDao,
) : DemoSeeder {

    override suspend fun seed(profileId: String) {
        val now = Instant.now()
        val today = LocalDate.now()
        val titles = if (DemoProfile.fromId(profileId) == DemoProfile.WORK) {
            listOf("Review PR #482", "Prep sprint demo", "1:1 with manager")
        } else {
            listOf("Buy groceries", "Call mom", "Gym session")
        }
        titles.forEachIndexed { index, title ->
            dao.upsertTask(
                TaskEntity(
                    id = "${profileId}_task_${index + 1}", title = title, description = "", date = today,
                    completed = index == 0, createdAt = now, profileId = profileId,
                ),
            )
        }
    }

    override suspend fun clear(profileId: String) {
        dao.deleteAllByProfile(profileId)
    }
}
