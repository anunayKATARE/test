package com.flappybird.game.engine

import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.entity.Pipe
import com.flappybird.game.domain.physics.ReachabilityCalculator
import kotlin.random.Random

/** Spawning strategy abstraction — a future "increasing difficulty" or
 *  "fixed pattern" spawner can be dropped in without touching [GameEngine] (OCP). */
interface PipeSpawner {
    fun spawn(): Pipe

    /** Clears any memory of previously spawned pipes, e.g. on a new game. */
    fun reset()
}

/**
 * Picks each gap center at random, but constrained to stay within what the
 * bird can physically reach from the previous pipe's gap before it arrives at
 * this one — see [ReachabilityCalculator]. Without this constraint, two
 * consecutive pipes could require more climb than the bird's jump strength
 * and the fastest humanly possible tap rate can deliver in the time between
 * them, making the game unfairly impossible at that point.
 */
class RandomPipeSpawner(
    private val config: GameConfig,
    private val reachabilityCalculator: ReachabilityCalculator,
    private val random: Random = Random.Default
) : PipeSpawner {

    private var previousGapCenterY: Float? = null

    override fun spawn(): Pipe {
        val playableHeight = config.worldHeight - config.groundHeight
        val minCenter = config.pipeGap
        val maxCenter = playableHeight - config.pipeGap
        val gapCenterY = nextGapCenterY(minCenter, maxCenter)
        previousGapCenterY = gapCenterY
        return Pipe(config = config, startX = config.worldWidth, gapCenterY = gapCenterY)
    }

    override fun reset() {
        previousGapCenterY = null
    }

    private fun nextGapCenterY(minCenter: Float, maxCenter: Float): Float {
        if (maxCenter <= minCenter) return (minCenter + maxCenter) / 2f

        val previous = previousGapCenterY ?: return random.nextFloat() * (maxCenter - minCenter) + minCenter

        val travelTime = config.pipeSpawnIntervalSeconds
        val reachableLow = (previous - reachabilityCalculator.maxAscent(travelTime)).coerceAtLeast(minCenter)
        val reachableHigh = (previous + reachabilityCalculator.maxDescent(travelTime)).coerceAtMost(maxCenter)

        if (reachableLow >= reachableHigh) return reachableLow.coerceIn(minCenter, maxCenter)
        return random.nextFloat() * (reachableHigh - reachableLow) + reachableLow
    }
}
