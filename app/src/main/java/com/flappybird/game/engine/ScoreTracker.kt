package com.flappybird.game.engine

/** Isolated scoring responsibility — the engine doesn't need to know how score
 *  is stored or persisted, only that it can be incremented/read/reset (SRP/DIP). */
interface ScoreTracker {
    val score: Int
    fun increment()
    fun reset()
}

class InMemoryScoreTracker : ScoreTracker {
    override var score: Int = 0
        private set

    override fun increment() {
        score += 1
    }

    override fun reset() {
        score = 0
    }
}
