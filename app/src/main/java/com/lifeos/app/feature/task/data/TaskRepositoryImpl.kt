package com.lifeos.app.feature.task.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val demoModeRepository: DemoModeRepository,
) : TaskRepository {

    private fun Flow<List<TaskEntity>>.filterByActiveProfile(): Flow<List<TaskEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeTasksForDate(date: LocalDate): Flow<List<Task>> =
        dao.observeForDate(date.toEpochDay()).filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeTasksInRange(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        dao.observeInRange(start.toEpochDay(), end.toEpochDay()).filterByActiveProfile()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(id: String): Task? = dao.getById(id)?.toDomain()

    override suspend fun upsertTask(task: Task) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsertTask(task.toEntity().copy(profileId = profileId))
    }

    override suspend fun deleteTask(id: String) = dao.deleteTask(id)

    override suspend fun setCompleted(id: String, completed: Boolean) = dao.setCompleted(id, completed)

    override suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.completionCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }

    override suspend fun totalCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.totalCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }
}
