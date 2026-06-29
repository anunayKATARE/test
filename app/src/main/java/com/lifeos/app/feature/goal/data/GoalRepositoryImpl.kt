package com.lifeos.app.feature.goal.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.goal.domain.Goal
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.goal.domain.GoalStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao,
    private val demoModeRepository: DemoModeRepository,
) : GoalRepository {

    private fun Flow<List<GoalEntity>>.filterByActiveProfile(): Flow<List<GoalEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeActiveGoals(): Flow<List<Goal>> =
        dao.observeActive().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeAllGoals(): Flow<List<Goal>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeGoalsByHorizon(horizon: GoalHorizon): Flow<List<Goal>> =
        dao.observeByHorizon(horizon).filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getGoal(id: String): Goal? = dao.getById(id)?.toDomain()

    override suspend fun upsert(goal: Goal) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(goal.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun setStatus(id: String, status: GoalStatus) = dao.setStatus(id, status)
}
