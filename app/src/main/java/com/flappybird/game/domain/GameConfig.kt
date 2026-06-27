package com.flappybird.game.domain

/**
 * Centralised tuning constants. Pulling these out of the entities/engine means
 * gameplay can be re-balanced without touching logic (Open/Closed Principle).
 */
data class GameConfig(
    val worldWidth: Float,
    val worldHeight: Float,
    val gravity: Float = 1400f,
    val jumpVelocity: Float = -480f,
    val maxFallSpeed: Float = 900f,
    val birdRadius: Float = 24f,
    val birdStartX: Float = 0f,
    val pipeWidth: Float = 110f,
    val pipeGap: Float = 320f,
    val pipeSpeed: Float = 260f,
    val pipeSpawnIntervalSeconds: Float = 1.6f,
    val groundHeight: Float = 80f,
    /** Generous upper bound on sustained single-finger tap rate, used to keep
     *  procedurally placed pipe gaps reachable — see [PhysicsReachabilityCalculator]. */
    val maxTapsPerSecond: Float = 8f
) {
    companion object {
        fun forScreen(width: Float, height: Float) = GameConfig(
            worldWidth = width,
            worldHeight = height,
            birdStartX = width * 0.3f
        )
    }
}
