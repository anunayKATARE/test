package com.flappybird.game.domain

/**
 * Small, focused contracts (Interface Segregation Principle) instead of one
 * monolithic "GameObject" interface. Entities implement only what applies to them.
 */

/** A 2D axis-aligned bounding box used for collision checks. */
data class Bounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun overlaps(other: Bounds): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

interface Updatable {
    fun update(deltaSeconds: Float)
}

interface Collidable {
    val bounds: Bounds
}

/** Marker for anything that can be drawn; actual drawing is delegated to a GameRenderer
 *  implementation so entities never depend on an Android Canvas directly (DIP). */
interface Renderable

interface GameEntity : Updatable, Renderable

enum class GameState {
    READY, PLAYING, GAME_OVER
}
