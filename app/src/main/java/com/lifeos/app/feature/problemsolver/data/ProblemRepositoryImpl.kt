package com.lifeos.app.feature.problemsolver.data

import com.lifeos.app.feature.problemsolver.domain.Problem
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProblemRepositoryImpl @Inject constructor(
    private val dao: ProblemDao,
) : ProblemRepository {

    override fun observeAll(): Flow<List<Problem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOpen(): Flow<List<Problem>> =
        dao.observeOpen().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Problem? = dao.getById(id)?.toDomain()

    override suspend fun upsert(problem: Problem) = dao.upsert(problem.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun setStatus(id: String, status: ProblemStatus) = dao.setStatus(id, status)
}
