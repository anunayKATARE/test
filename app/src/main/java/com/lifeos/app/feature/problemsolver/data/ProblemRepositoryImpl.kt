package com.lifeos.app.feature.problemsolver.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProblemRepositoryImpl @Inject constructor(
    private val dao: ProblemDao,
    private val demoModeRepository: DemoModeRepository,
) : ProblemRepository {

    private fun Flow<List<ProblemEntity>>.filterByActiveProfile(): Flow<List<ProblemEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeAll(): Flow<List<Problem>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeOpen(): Flow<List<Problem>> =
        dao.observeOpen().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Problem? = dao.getById(id)?.toDomain()

    override suspend fun upsert(problem: Problem) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(problem.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun setStatus(id: String, status: ProblemStatus) = dao.setStatus(id, status)
}
