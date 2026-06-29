package com.lifeos.app.feature.selfbelief.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefReflection
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SelfBeliefRepositoryImpl @Inject constructor(
    private val dao: SelfBeliefDao,
    private val demoModeRepository: DemoModeRepository,
) : SelfBeliefRepository {

    override fun observeAll(): Flow<List<SelfBeliefReflection>> =
        dao.observeAll()
            .combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): SelfBeliefReflection? = dao.getById(id)?.toDomain()

    override suspend fun upsert(reflection: SelfBeliefReflection) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsert(reflection.toEntity().copy(profileId = profileId))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun allStrengths(): List<String> = dao.allStrengths()
}
