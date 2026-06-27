package com.flappybird.game.domain.entity

import com.flappybird.game.domain.Bounds
import com.flappybird.game.domain.Collidable
import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.GameEntity
import com.flappybird.game.domain.physics.PhysicsEngine

/**
 * The player-controlled entity. Bird only knows about its own state transitions;
 * it asks an injected [PhysicsEngine] how velocity evolves rather than hard-coding
 * a gravity formula itself (Single Responsibility + Dependency Inversion).
 */
class Bird(
    private val config: GameConfig,
    private val physicsEngine: PhysicsEngine
) : GameEntity, Collidable {

    var x: Float = config.birdStartX
        private set
    var y: Float = config.worldHeight / 2f
        private set
    var velocityY: Float = 0f
        private set

    override val bounds: Bounds
        get() = Bounds(
            left = x - config.birdRadius,
            top = y - config.birdRadius,
            right = x + config.birdRadius,
            bottom = y + config.birdRadius
        )

    override fun update(deltaSeconds: Float) {
        velocityY = physicsEngine.applyGravity(velocityY, deltaSeconds)
        y += velocityY * deltaSeconds
    }

    fun jump() {
        velocityY = config.jumpVelocity
    }

    fun hasHitGround(): Boolean = y + config.birdRadius >= config.worldHeight - config.groundHeight

    fun hasHitCeiling(): Boolean = y - config.birdRadius <= 0f

    fun reset() {
        x = config.birdStartX
        y = config.worldHeight / 2f
        velocityY = 0f
    }
}
