package com.elementinspector.app.accessibility.overlay

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.elementinspector.app.R
import com.elementinspector.app.ui.common.AttributeListBinder
import com.elementinspector.domain.model.ElementNode
import com.elementinspector.domain.model.ElementSelection
import com.elementinspector.domain.resolver.ElementAtPointResolver

/**
 * Owns every window the accessibility service adds on top of the screen: the
 * draggable bubble and the full-screen inspect overlay. Kept separate from
 * [com.elementinspector.app.accessibility.InspectorAccessibilityService] so the
 * service itself stays a thin lifecycle shell — all window/view plumbing lives
 * here, and can be extended with new overlay types without touching the service.
 */
class OverlayManager(private val service: AccessibilityService) {

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val inflater = LayoutInflater.from(service)

    private var bubbleView: View? = null
    private var inspectRoot: FrameLayout? = null

    fun showBubble(onTap: () -> Unit) {
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
        BubbleDragController(view, windowManager, params, onTap = onTap).attach()
        windowManager.addView(view, params)
        bubbleView = view
    }

    fun hideBubble() {
        bubbleView?.let { runCatching { windowManager.removeView(it) } }
        bubbleView = null
    }

    fun showInspectOverlay(
        screenshot: Bitmap,
        tree: ElementNode,
        resolver: ElementAtPointResolver,
        onSave: (List<ElementSelection>) -> Unit,
        onDiscard: () -> Unit,
    ) {
        hideInspectOverlay()

        val root = inflater.inflate(R.layout.overlay_inspect, null) as FrameLayout
        val screenshotContainer = root.findViewById<FrameLayout>(R.id.screenshotContainer)
        val selectionCountText = root.findViewById<TextView>(R.id.selectionCountText)
        val attributePanel = root.findViewById<View>(R.id.attributePanel)
        val attributeListContainer = root.findViewById<LinearLayout>(R.id.attributeListContainer)
        val saveButton = root.findViewById<Button>(R.id.saveButton)
        val discardButton = root.findViewById<Button>(R.id.discardButton)

        val screenshotView = InspectOverlayView(service, screenshot, tree, resolver) { selections, focused ->
            selectionCountText.text = service.getString(R.string.overlay_selection_count, selections.size)
            saveButton.isEnabled = selections.isNotEmpty()
            if (focused != null) {
                attributePanel.visibility = View.VISIBLE
                AttributeListBinder.bind(attributeListContainer, focused.attributeSummary)
            }
        }
        screenshotContainer.addView(
            screenshotView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )

        saveButton.setOnClickListener { onSave(screenshotView.currentSelections()) }
        discardButton.setOnClickListener { onDiscard() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            0,
            PixelFormat.TRANSLUCENT,
        )
        windowManager.addView(root, params)
        inspectRoot = root
    }

    fun hideInspectOverlay() {
        inspectRoot?.let { runCatching { windowManager.removeView(it) } }
        inspectRoot = null
    }

    fun destroyAll() {
        hideBubble()
        hideInspectOverlay()
    }
}
