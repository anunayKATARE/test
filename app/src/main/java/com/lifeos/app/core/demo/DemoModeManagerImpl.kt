package com.lifeos.app.core.demo

import javax.inject.Inject
import kotlinx.coroutines.flow.first

class DemoModeManagerImpl @Inject constructor(
    private val demoModeRepository: DemoModeRepository,
    private val seeders: Set<@JvmSuppressWildcards DemoSeeder>,
    private val tourState: DemoTourState,
) : DemoModeManager {

    override suspend fun activate(profile: DemoProfile) {
        seeders.forEach { it.clear(profile.id) }
        seeders.forEach { it.seed(profile.id) }
        demoModeRepository.setActiveProfile(profile)
        tourState.start()
    }

    override suspend fun deactivate() {
        val active = demoModeRepository.activeProfile.first() ?: return
        seeders.forEach { it.clear(active.id) }
        demoModeRepository.setActiveProfile(null)
        tourState.skipTour()
    }
}
