package com.lifeos.app.feature.selfbelief.domain

import com.lifeos.app.core.common.IdGenerator
import java.time.Instant
import javax.inject.Inject

class AddSelfBeliefReflectionUseCase @Inject constructor(
    private val repository: SelfBeliefRepository,
) {
    suspend operator fun invoke(
        whatHappened: String,
        storyTelling: String,
        evidenceFor: String,
        evidenceAgainst: String,
        friendAdvice: String,
        strengthsThatRemain: String,
        nextSmallAction: String,
        dateTime: Instant = Instant.now(),
    ): SelfBeliefReflection {
        val reflection = SelfBeliefReflection(
            id = IdGenerator.newId(),
            dateTime = dateTime,
            whatHappened = whatHappened,
            storyTelling = storyTelling,
            evidenceFor = evidenceFor,
            evidenceAgainst = evidenceAgainst,
            friendAdvice = friendAdvice,
            strengthsThatRemain = strengthsThatRemain,
            nextSmallAction = nextSmallAction,
        )
        repository.upsert(reflection)
        return reflection
    }
}

class DeleteSelfBeliefReflectionUseCase @Inject constructor(
    private val repository: SelfBeliefRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

/** Builds the running "evidence base" of strengths the user has reaffirmed across reflections. */
class BuildStrengthsEvidenceBaseUseCase @Inject constructor(
    private val repository: SelfBeliefRepository,
) {
    suspend operator fun invoke(): List<String> =
        repository.allStrengths().filter { it.isNotBlank() }.distinct()
}
