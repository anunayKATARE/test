package com.lifeos.app.feature.checkin.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.checkin.domain.CheckInCommitment
import com.lifeos.app.feature.checkin.domain.CheckInRepository
import com.lifeos.app.feature.checkin.domain.CheckInSession
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CheckInRepositoryImpl @Inject constructor(
    private val dao: CheckInDao,
    private val demoModeRepository: DemoModeRepository,
) : CheckInRepository {

    override fun observeActive(): Flow<CheckInSession?> =
        combine(dao.observeActive(), demoModeRepository.activeProfile) { row, profile ->
            if (row == null || row.session.profileId != profile?.id) null else row.toDomain()
        }

    override suspend fun upsert(session: CheckInSession) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsertSession(session.toSessionEntity(profileId))
        dao.deleteCommitmentsForSession(session.id)
        dao.upsertCommitments(session.commitments.mapIndexed { i, c -> c.toEntity(session.id, i) })
    }

    override suspend fun complete(id: String) {
        dao.setCompleted(id, Instant.now().toEpochMilli())
    }
}
