package com.flappybird.game.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.GameState
import com.flappybird.game.domain.entity.Bird
import com.flappybird.game.domain.entity.Pipe

/**
 * Concrete [GameRenderer] that draws with [android.graphics.Canvas]. This is the
 * only class in the project allowed to know about Android drawing primitives —
 * everything upstream (engine, entities) stays platform-agnostic.
 */
class CanvasGameRenderer(private val config: GameConfig) : GameRenderer {

    private var canvas: Canvas? = null

    private val skyPaint = Paint().apply { color = Color.rgb(78, 192, 202) }
    private val groundPaint = Paint().apply { color = Color.rgb(222, 184, 99) }
    private val birdPaint = Paint().apply { color = Color.rgb(255, 204, 0) }
    private val pipePaint = Paint().apply { color = Color.rgb(70, 170, 80) }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 72f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val hintPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    fun attachCanvas(canvas: Canvas) {
        this.canvas = canvas
    }

    override fun render(bird: Bird, pipes: List<Pipe>, score: Int, state: GameState) {
        val c = canvas ?: return

        c.drawRect(0f, 0f, config.worldWidth, config.worldHeight, skyPaint)

        for (pipe in pipes) {
            val top = pipe.topBounds
            val bottom = pipe.bottomBounds
            c.drawRect(top.left, top.top, top.right, top.bottom, pipePaint)
            c.drawRect(bottom.left, bottom.top, bottom.right, bottom.bottom, pipePaint)
        }

        c.drawRect(
            0f,
            config.worldHeight - config.groundHeight,
            config.worldWidth,
            config.worldHeight,
            groundPaint
        )

        c.drawCircle(bird.x, bird.y, config.birdRadius, birdPaint)

        c.drawText(score.toString(), config.worldWidth / 2f, 120f, textPaint)

        when (state) {
            GameState.READY -> c.drawText(
                "Tap to start",
                config.worldWidth / 2f,
                config.worldHeight / 2f - 80f,
                hintPaint
            )
            GameState.GAME_OVER -> c.drawText(
                "Game Over - tap to retry",
                config.worldWidth / 2f,
                config.worldHeight / 2f - 80f,
                hintPaint
            )
            GameState.PLAYING -> Unit
        }
    }
}
