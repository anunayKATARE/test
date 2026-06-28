package com.lifeos.app.feature.task.data

import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
) : TaskRepository {

    override fun observeTasksForDate(date: LocalDate): Flow<List<Task>> =
        dao.observeForDate(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun observeTasksInRange(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        dao.observeInRange(start.toEpochDay(), end.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(id: String): Task? = dao.getById(id)?.toDomain()

    override suspend fun upsertTask(task: Task) = dao.upsertTask(task.toEntity())

    override suspend fun deleteTask(id: String) = dao.deleteTask(id)

    override suspend fun setCompleted(id: String, completed: Boolean) = dao.setCompleted(id, completed)

    override suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.completionCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }

    override suspend fun totalCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.totalCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }
}
