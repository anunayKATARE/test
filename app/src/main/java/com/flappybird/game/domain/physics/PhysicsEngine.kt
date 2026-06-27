package com.flappybird.game.domain.physics

/**
 * Abstraction over "how velocity/position change over time" so entities depend on
 * a contract, not a concrete formula (Dependency Inversion). Swapping in a different
 * physics model later (e.g. terminal-velocity tuning, different gravity curve) means
 * adding a new implementation, not editing Bird (Open/Closed).
 */
interface PhysicsEngine {
    /** Returns the new vertical velocity after gravity is applied for [deltaSeconds]. */
    fun applyGravity(currentVelocity: Float, deltaSeconds: Float): Float
}

class StandardPhysicsEngine(
    private val gravity: Float,
    private val maxFallSpeed: Float
) : PhysicsEngine {
    override fun applyGravity(currentVelocity: Float, deltaSeconds: Float): Float {
        val updated = currentVelocity + gravity * deltaSeconds
        return updated.coerceAtMost(maxFallSpeed)
    }
}
