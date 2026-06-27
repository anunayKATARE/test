package com.flappybird.game.engine

import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.entity.Pipe
import kotlin.random.Random

/** Spawning strategy abstraction — a future "increasing difficulty" or
 *  "fixed pattern" spawner can be dropped in without touching [GameEngine] (OCP). */
interface PipeSpawner {
    fun spawn(): Pipe
}

class RandomPipeSpawner(
    private val config: GameConfig,
    private val random: Random = Random.Default
) : PipeSpawner {
    override fun spawn(): Pipe {
        val playableHeight = config.worldHeight - config.groundHeight
        val minCenter = config.pipeGap
        val maxCenter = playableHeight - config.pipeGap
        val gapCenterY = if (maxCenter > minCenter) {
            random.nextFloat() * (maxCenter - minCenter) + minCenter
        } else {
            playableHeight / 2f
        }
        return Pipe(config = config, startX = config.worldWidth, gapCenterY = gapCenterY)
    }
}
