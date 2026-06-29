package com.lifeos.app.core.demo

/**
 * Implemented once per feature module. [DemoModeManager] discovers every seeder through Hilt
 * multibinding, so adding a new feature's demo data never requires touching existing seeders.
 */
interface DemoSeeder {
    suspend fun seed(profileId: String)
    suspend fun clear(profileId: String)
}
