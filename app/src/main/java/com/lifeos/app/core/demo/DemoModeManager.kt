package com.lifeos.app.core.demo

interface DemoModeManager {
    suspend fun activate(profile: DemoProfile)
    suspend fun deactivate()
}
