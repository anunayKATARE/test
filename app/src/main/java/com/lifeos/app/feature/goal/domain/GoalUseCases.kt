package com.lifeos.app.feature.goal.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AddGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        horizon: GoalHorizon = GoalHorizon.WEEKLY,
        targetDate: LocalDate? = null,
        linkedHabitIds: List<String> = emptyList(),
        linkedJournalIds: List<String> = emptyList(),
        categoryId: String? = null,
    ): Goal {
        val goal = Goal(
            id = IdGenerator.newId(),
            title = title,
            description = description,
            horizon = horizon,
            targetDate = targetDate,
            linkedHabitIds = linkedHabitIds,
            linkedJournalIds = linkedJournalIds,
            categoryId = categoryId,
        )
        repository.upsert(goal)
        return goal
    }
}

class EditGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goal: Goal) = repository.upsert(goal)
}

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

class CompleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String) {
        val goal = repository.getGoal(id) ?: return
        repository.upsert(goal.copy(status = GoalStatus.COMPLETED, completedAt = Instant.now()))
    }
}

class ArchiveGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String) = repository.setStatus(id, GoalStatus.ARCHIVED)
}

class GetTodaysGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(): List<Goal> = repository.observeGoalsByHorizon(GoalHorizon.TODAY).first()
}
