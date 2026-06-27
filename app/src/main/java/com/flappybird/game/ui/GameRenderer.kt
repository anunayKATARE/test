package com.flappybird.game.ui

import com.flappybird.game.domain.GameState
import com.flappybird.game.domain.entity.Bird
import com.flappybird.game.domain.entity.Pipe

/**
 * Rendering abstraction (DIP): [GameView] depends on this interface, not on a
 * concrete drawing implementation. A future renderer (OpenGL, Compose Canvas,
 * a test "headless" renderer) can be substituted without touching the engine
 * or the view's input handling.
 */
interface GameRenderer {
    fun render(bird: Bird, pipes: List<Pipe>, score: Int, state: GameState)
}
