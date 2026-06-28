package com.lifeos.app.feature.problemsolver.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class RegisterProblemUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        possibleCauses: List<String> = emptyList(),
    ): Problem {
        val problem = Problem(
            id = IdGenerator.newId(),
            title = title,
            description = description,
            possibleCauses = possibleCauses,
        )
        repository.upsert(problem)
        return problem
    }
}

class RecordAttemptUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(problemId: String, attempt: String, worked: Boolean?) {
        val problem = repository.getById(problemId) ?: return
        val updated = problem.copy(
            attemptsMade = problem.attemptsMade + attempt,
            whatWorked = if (worked == true) listOf(problem.whatWorked, attempt).filter { it.isNotBlank() }.joinToString("; ") else problem.whatWorked,
            whatFailed = if (worked == false) listOf(problem.whatFailed, attempt).filter { it.isNotBlank() }.joinToString("; ") else problem.whatFailed,
            updatedAt = Instant.now(),
        )
        repository.upsert(updated)
    }
}

class UpdateProblemUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(problem: Problem) = repository.upsert(problem.copy(updatedAt = Instant.now()))
}

class SetProblemStatusUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(id: String, status: ProblemStatus) = repository.setStatus(id, status)
}

class DeleteProblemUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

class GetOpenProblemsUseCase @Inject constructor(
    private val repository: ProblemRepository,
) {
    suspend operator fun invoke(): List<Problem> = repository.observeOpen().first()
}
