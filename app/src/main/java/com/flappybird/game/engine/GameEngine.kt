package com.flappybird.game.engine

import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.GameState
import com.flappybird.game.domain.entity.Bird
import com.flappybird.game.domain.entity.Pipe
import com.flappybird.game.domain.physics.CollisionDetector

/**
 * Orchestrates a single game session. Depends only on abstractions
 * ([CollisionDetector], [PipeSpawner], [ScoreTracker]) injected through the
 * constructor — never instantiates a concrete implementation itself
 * (Dependency Inversion). This is what lets [GameEngine] be unit-tested with
 * fakes and lets the UI layer remain a thin renderer/input adapter.
 */
class GameEngine(
    private val config: GameConfig,
    val bird: Bird,
    private val collisionDetector: CollisionDetector,
    private val pipeSpawner: PipeSpawner,
    private val scoreTracker: ScoreTracker
) {
    var state: GameState = GameState.READY
        private set

    private val _pipes = mutableListOf<Pipe>()
    val pipes: List<Pipe> get() = _pipes

    val score: Int get() = scoreTracker.score

    private var timeSinceLastSpawn = 0f

    fun onTap() {
        when (state) {
            GameState.READY -> {
                state = GameState.PLAYING
                bird.jump()
            }
            GameState.PLAYING -> bird.jump()
            GameState.GAME_OVER -> restart()
        }
    }

    fun update(deltaSeconds: Float) {
        if (state != GameState.PLAYING) return

        bird.update(deltaSeconds)
        updatePipes(deltaSeconds)

        if (isColliding()) {
            state = GameState.GAME_OVER
        }
    }

    private fun updatePipes(deltaSeconds: Float) {
        timeSinceLastSpawn += deltaSeconds
        if (timeSinceLastSpawn >= config.pipeSpawnIntervalSeconds) {
            timeSinceLastSpawn = 0f
            _pipes.add(pipeSpawner.spawn())
        }

        val iterator = _pipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.update(deltaSeconds)

            if (pipe.isPastBird(bird.x)) {
                pipe.scored = true
                scoreTracker.increment()
            }
            if (pipe.isOffScreen()) {
                iterator.remove()
            }
        }
    }

    private fun isColliding(): Boolean {
        if (bird.hasHitGround() || bird.hasHitCeiling()) return true
        return _pipes.any { pipe ->
            collisionDetector.isColliding(bird, pipe) && pipe.collidesWithBird(bird)
        }
    }

    private fun restart() {
        bird.reset()
        _pipes.clear()
        pipeSpawner.reset()
        scoreTracker.reset()
        timeSinceLastSpawn = 0f
        state = GameState.READY
    }
}
