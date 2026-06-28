package com.lifeos.app.feature.task.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.LocalDate
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(title: String, date: LocalDate, description: String = ""): Task {
        val task = Task(
            id = IdGenerator.newId(),
            title = title,
            description = description,
            date = date,
        )
        repository.upsertTask(task)
        return task
    }
}

class EditTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(task: Task) = repository.upsertTask(task)
}

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteTask(id)
}

class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: String, completed: Boolean) = repository.setCompleted(id, completed)
}
