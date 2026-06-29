package com.lifeos.app.core.demo

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for which [DemoProfile] is currently active, if any. A null active
 * profile means the app is showing the user's real data.
 */
interface DemoModeRepository {
    val activeProfile: Flow<DemoProfile?>
    suspend fun setActiveProfile(profile: DemoProfile?)
}
