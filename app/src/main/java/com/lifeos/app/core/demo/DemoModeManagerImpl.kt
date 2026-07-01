package com.lifeos.app.core.demo

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class DemoModeManagerImpl @Inject constructor(
    private val demoModeRepository: DemoModeRepository,
    private val seeders: Set<@JvmSuppressWildcards DemoSeeder>,
    private val tourState: DemoTourState,
) : DemoModeManager {

    override suspend fun activateDemoTemplate(template: DemoTemplate): Profile {
        val anchor = Instant.now()
        val profile = Profile(
            id = template.id,
            name = template.label,
            isDemo = true,
            demoTemplate = template,
            createdAt = anchor,
        )
        seeders.forEach { it.clear(profile.id) }
        seeders.forEach { it.seed(profile.id, template, anchor) }
        demoModeRepository.upsertProfile(profile)
        demoModeRepository.setActiveProfile(profile)
        tourState.start()
        return profile
    }

    override suspend fun activateProfile(profile: Profile) {
        demoModeRepository.setActiveProfile(profile)
    }

    override suspend fun createUserProfile(name: String): Profile {
        return demoModeRepository.createUserProfile(name)
    }

    override suspend fun deleteProfile(profileId: String) {
        val active = demoModeRepository.activeProfile.first()
        if (active?.id == profileId) demoModeRepository.setActiveProfile(null)
        demoModeRepository.deleteProfile(profileId)
    }

    override suspend fun deactivate() {
        val active = demoModeRepository.activeProfile.first() ?: return
        if (active.isDemo) seeders.forEach { it.clear(active.id) }
        demoModeRepository.setActiveProfile(null)
        tourState.skipTour()
    }
}
