package com.elementinspector.app.accessibility.overlay

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.elementinspector.app.R

/**
 * Owns the floating bubble window the accessibility service adds on top of
 * the screen. Kept separate from
 * [com.elementinspector.app.accessibility.InspectorAccessibilityService] so the
 * service itself stays a thin lifecycle shell. Exploring and editing a
 * capture happens later, inside the Captured tab — this class only ever
 * needs to manage the one always-on-top bubble window.
 */
class OverlayManager(private val service: AccessibilityService) {

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val inflater = LayoutInflater.from(service)

    private var bubbleView: View? = null

    fun showBubble(onTap: () -> Unit, onDismiss: () -> Unit = {}) {
        if (bubbleView != null) return
        val view = inflater.inflate(R.layout.overlay_bubble, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }
        BubbleDragController(
            view = view,
            windowManager = windowManager,
            params = params,
            onTap = onTap,
            onDismiss = { hideBubble(); onDismiss() },
        ).attach()
        windowManager.addView(view, params)
        bubbleView = view
    }

    fun hideBubble() {
        bubbleView?.let { runCatching { windowManager.removeView(it) } }
        bubbleView = null
    }

    fun destroyAll() {
        hideBubble()
    }
}
