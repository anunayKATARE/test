package com.elementinspector.app.accessibility.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/** Lets the floating bubble be dragged anywhere on screen while still
 *  distinguishing a drag from a simple tap, which triggers [onTap]. */
class BubbleDragController(
    private val view: View,
    private val windowManager: WindowManager,
    private val params: WindowManager.LayoutParams,
    private val onTap: () -> Unit,
) {
    private var downRawX = 0f
    private var downRawY = 0f
    private var downParamX = 0
    private var downParamY = 0
    private var moved = false

    fun attach() {
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downParamX = params.x
                    downParamY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).toInt()
                    val dy = (event.rawY - downRawY).toInt()
                    if (abs(dx) > DRAG_THRESHOLD_PX || abs(dy) > DRAG_THRESHOLD_PX) moved = true
                    params.x = downParamX + dx
                    params.y = downParamY + dy
                    runCatching { windowManager.updateViewLayout(view, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) onTap()
                    true
                }
                else -> false
            }
        }
    }

    private companion object {
        const val DRAG_THRESHOLD_PX = 12
    }
}
