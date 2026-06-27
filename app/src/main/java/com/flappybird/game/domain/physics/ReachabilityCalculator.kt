package com.flappybird.game.domain.physics

/**
 * Answers "how far could the bird realistically move up or down in a given
 * amount of time", using the same gravity/jump model [Bird][com.flappybird.game.domain.entity.Bird]
 * itself is driven by, plus an assumed upper bound on human tap rate.
 *
 * [com.flappybird.game.engine.PipeSpawner] depends on this abstraction rather
 * than the formulas themselves (DIP), so obstacle placement always stays
 * within what a player can physically achieve, and a different skill
 * assumption or physics model can be swapped in without touching the spawner
 * (OCP).
 */
interface ReachabilityCalculator {
    /** Furthest the bird can climb in [durationSeconds] of continuous max-rate tapping. */
    fun maxAscent(durationSeconds: Float): Float

    /** Furthest the bird can fall in [durationSeconds] if the player taps nothing. */
    fun maxDescent(durationSeconds: Float): Float
}

class PhysicsReachabilityCalculator(
    private val gravity: Float,
    private val jumpVelocity: Float,
    private val maxFallSpeed: Float,
    private val maxTapsPerSecond: Float
) : ReachabilityCalculator {

    override fun maxAscent(durationSeconds: Float): Float {
        val tapInterval = 1f / maxTapsPerSecond
        // Each tap resets velocity to jumpVelocity; gravity pulls it back down
        // until the next tap. This is the average velocity of that sawtooth at
        // the fastest tap rate a human can sustain.
        val averageVelocity = jumpVelocity + 0.5f * gravity * tapInterval
        return (-averageVelocity * durationSeconds).coerceAtLeast(0f)
    }

    override fun maxDescent(durationSeconds: Float): Float {
        val timeToTerminalVelocity = maxFallSpeed / gravity
        return if (durationSeconds <= timeToTerminalVelocity) {
            0.5f * gravity * durationSeconds * durationSeconds
        } else {
            val accelerationPhase = 0.5f * gravity * timeToTerminalVelocity * timeToTerminalVelocity
            val terminalPhase = maxFallSpeed * (durationSeconds - timeToTerminalVelocity)
            accelerationPhase + terminalPhase
        }
    }
}
