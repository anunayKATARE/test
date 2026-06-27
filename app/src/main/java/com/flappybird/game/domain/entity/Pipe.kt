package com.flappybird.game.domain.entity

import com.flappybird.game.domain.Bounds
import com.flappybird.game.domain.Collidable
import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.GameEntity

/**
 * A pipe pair (top + bottom obstacle with a gap). Implements [GameEntity] like [Bird]
 * does (Liskov Substitution: both can sit in the same `List<GameEntity>` and be
 * updated/rendered polymorphically without special-casing).
 */
class Pipe(
    private val config: GameConfig,
    startX: Float,
    val gapCenterY: Float
) : GameEntity, Collidable {

    var x: Float = startX
        private set

    var scored: Boolean = false

    val topBounds: Bounds
        get() = Bounds(
            left = x,
            top = 0f,
            right = x + config.pipeWidth,
            bottom = gapCenterY - config.pipeGap / 2f
        )

    val bottomBounds: Bounds
        get() = Bounds(
            left = x,
            top = gapCenterY + config.pipeGap / 2f,
            right = x + config.pipeWidth,
            bottom = config.worldHeight - config.groundHeight
        )

    /** [Collidable.bounds] exposes the full pipe footprint; fine-grained checks
     *  against the two segments happen in [collidesWithBird]. */
    override val bounds: Bounds
        get() = Bounds(x, topBounds.top, x + config.pipeWidth, bottomBounds.bottom)

    fun collidesWithBird(bird: Bird): Boolean =
        bird.bounds.overlaps(topBounds) || bird.bounds.overlaps(bottomBounds)

    fun isOffScreen(): Boolean = x + config.pipeWidth < 0f

    fun isPastBird(birdX: Float): Boolean = !scored && x + config.pipeWidth < birdX

    override fun update(deltaSeconds: Float) {
        x -= config.pipeSpeed * deltaSeconds
    }
}
