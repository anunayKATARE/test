package com.elementinspector.app.accessibility.overlay

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/** Lets the floating bubble be dragged anywhere on screen while still
 *  distinguishing a drag from a simple tap, which triggers [onTap], or a
 *  long press (held in place past [LONG_PRESS_MS]), which triggers
 *  [onDismiss] so the user has a way to remove the bubble entirely. */
class BubbleDragController(
    private val view: View,
    private val windowManager: WindowManager,
    private val params: WindowManager.LayoutParams,
    private val onTap: () -> Unit,
    private val onDismiss: () -> Unit,
) {
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable {
        longPressTriggered = true
        onDismiss()
    }

    private var downRawX = 0f
    private var downRawY = 0f
    private var downParamX = 0
    private var downParamY = 0
    private var moved = false
    private var longPressTriggered = false

    fun attach() {
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downParamX = params.x
                    downParamY = params.y
                    moved = false
                    longPressTriggered = false
                    longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_MS)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).toInt()
                    val dy = (event.rawY - downRawY).toInt()
                    if (abs(dx) > DRAG_THRESHOLD_PX || abs(dy) > DRAG_THRESHOLD_PX) {
                        moved = true
                        longPressHandler.removeCallbacks(longPressRunnable)
                    }
                    params.x = downParamX + dx
                    params.y = downParamY + dy
                    runCatching { windowManager.updateViewLayout(view, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    longPressHandler.removeCallbacks(longPressRunnable)
                    if (!moved && !longPressTriggered) onTap()
                    true
                }
                else -> false
            }
        }
    }

    private companion object {
        const val DRAG_THRESHOLD_PX = 12
        const val LONG_PRESS_MS = 600L
    }
}
