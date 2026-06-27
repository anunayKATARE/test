package com.flappybird.game.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.flappybird.game.domain.GameConfig
import com.flappybird.game.domain.entity.Bird
import com.flappybird.game.domain.physics.AabbCollisionDetector
import com.flappybird.game.domain.physics.StandardPhysicsEngine
import com.flappybird.game.engine.GameEngine
import com.flappybird.game.engine.InMemoryScoreTracker
import com.flappybird.game.engine.RandomPipeSpawner

/**
 * The only place that touches both the Android view system and the game engine.
 * It is intentionally "thin": no gameplay rules live here, only
 *   1) driving the frame loop,
 *   2) translating touch events into [GameEngine.onTap],
 *   3) handing the canvas to the renderer.
 *
 * The engine and its collaborators are built here (acting as the composition
 * root for gameplay objects) because their sizing depends on the view's actual
 * pixel dimensions, which aren't known until layout — everything it wires
 * together is still chosen through constructor injection on the receiving end.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), Choreographer.FrameCallback {

    private var engine: GameEngine? = null
    private var renderer: CanvasGameRenderer? = null
    private var lastFrameTimeNanos: Long = 0L

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        val config = GameConfig.forScreen(w.toFloat(), h.toFloat())
        val physicsEngine = StandardPhysicsEngine(config.gravity, config.maxFallSpeed)
        val bird = Bird(config, physicsEngine)

        engine = GameEngine(
            config = config,
            bird = bird,
            collisionDetector = AabbCollisionDetector(),
            pipeSpawner = RandomPipeSpawner(config),
            scoreTracker = InMemoryScoreTracker()
        )
        renderer = CanvasGameRenderer(config)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this)
        super.onDetachedFromWindow()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) {
            val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
            engine?.update(deltaSeconds.coerceAtMost(0.05f))
            invalidate()
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            engine?.onTap()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val currentEngine = engine ?: return
        val currentRenderer = renderer ?: return
        currentRenderer.attachCanvas(canvas)
        currentRenderer.render(
            bird = currentEngine.bird,
            pipes = currentEngine.pipes,
            score = currentEngine.score,
            state = currentEngine.state
        )
    }
}
