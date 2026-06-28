package com.lifeos.app.feature.problemsolver.domain

import kotlinx.coroutines.flow.Flow

interface ProblemRepository {
    fun observeAll(): Flow<List<Problem>>
    fun observeOpen(): Flow<List<Problem>>
    suspend fun getById(id: String): Problem?
    suspend fun upsert(problem: Problem)
    suspend fun delete(id: String)
    suspend fun setStatus(id: String, status: ProblemStatus)
}
