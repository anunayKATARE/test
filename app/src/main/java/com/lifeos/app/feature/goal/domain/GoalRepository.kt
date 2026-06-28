package com.lifeos.app.feature.goal.domain

import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeActiveGoals(): Flow<List<Goal>>
    fun observeAllGoals(): Flow<List<Goal>>
    fun observeGoalsByHorizon(horizon: GoalHorizon): Flow<List<Goal>>
    suspend fun getGoal(id: String): Goal?
    suspend fun upsert(goal: Goal)
    suspend fun delete(id: String)
    suspend fun setStatus(id: String, status: GoalStatus)
}
