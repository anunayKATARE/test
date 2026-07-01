package com.lifeos.app.core.demo

import java.time.Instant

/**
 * Implemented once per feature module. [DemoModeManager] discovers every seeder through Hilt
 * multibinding, so adding a new feature's demo data never requires touching existing seeders.
 * [anchor] is the instant the demo was activated — all relative dates are computed from it.
 */
interface DemoSeeder {
    suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant)
    suspend fun clear(profileId: String)
}
