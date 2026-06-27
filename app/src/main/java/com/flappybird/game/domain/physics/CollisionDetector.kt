package com.flappybird.game.domain.physics

import com.flappybird.game.domain.Collidable

/** Strategy abstraction for collision testing (DIP) — AABB today, swappable later
 *  for circle/polygon checks without changing any caller. */
interface CollisionDetector {
    fun isColliding(a: Collidable, b: Collidable): Boolean
}

class AabbCollisionDetector : CollisionDetector {
    override fun isColliding(a: Collidable, b: Collidable): Boolean =
        a.bounds.overlaps(b.bounds)
}
