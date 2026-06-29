package com.elementinspector.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.elementinspector.app.ElementInspectorApp
import com.elementinspector.app.R
import com.elementinspector.app.accessibility.overlay.OverlayManager
import com.elementinspector.app.util.BitmapStorage
import com.elementinspector.domain.model.CaptureDetail
import com.elementinspector.domain.model.ElementNode
import com.elementinspector.domain.model.ElementSelection
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Thin lifecycle shell: wires up the overlay, the tree mapper and the
 * screenshot capturer, and drives the freeze -> inspect -> save/discard flow.
 * All rendering/window logic lives in [OverlayManager]; all persistence lives
 * behind [com.elementinspector.domain.repository.CaptureRepository].
 */
class InspectorAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayManager
    private lateinit var treeMapper: AccessibilityTreeMapper
    private lateinit var screenshotCapturer: ScreenshotCapturer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var lastSeenPackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
        treeMapper = DefaultAccessibilityTreeMapper()
        screenshotCapturer = AccessibilityScreenshotCapturer(this)
        overlayManager.showBubble(onTap = ::startCaptureSession)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        event.packageName?.toString()?.let { lastSeenPackageName = it }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        serviceScope.cancel()
        if (::overlayManager.isInitialized) overlayManager.destroyAll()
        super.onDestroy()
    }

    private fun startCaptureSession() {
        val root = rootInActiveWindow
        if (root == null) {
            Toast.makeText(this, getString(R.string.toast_no_active_window), Toast.LENGTH_SHORT).show()
            return
        }
        val tree = treeMapper.map(root)
        val sourcePackageName = lastSeenPackageName
        val resolver = (application as ElementInspectorApp).container.elementAtPointResolver

        serviceScope.launch {
            val screenshot = screenshotCapturer.capture()
            if (screenshot == null) {
                Toast.makeText(
                    this@InspectorAccessibilityService,
                    getString(R.string.toast_screenshot_failed),
                    Toast.LENGTH_SHORT,
                ).show()
                return@launch
            }
            overlayManager.showInspectOverlay(
                screenshot = screenshot,
                tree = tree,
                resolver = resolver,
                onSave = { selections -> saveCapture(screenshot, tree, sourcePackageName, selections) },
                onDiscard = { overlayManager.hideInspectOverlay() },
            )
        }
    }

    private fun saveCapture(
        screenshot: Bitmap,
        tree: ElementNode,
        sourcePackageName: String?,
        selections: List<ElementSelection>,
    ) {
        val container = (application as ElementInspectorApp).container
        serviceScope.launch {
            val id = UUID.randomUUID().toString()
            val screenshotPath = BitmapStorage.saveScreenshot(this@InspectorAccessibilityService, id, screenshot)
            val detail = CaptureDetail(
                id = id,
                timestamp = System.currentTimeMillis(),
                screenshotPath = screenshotPath,
                sourcePackageName = sourcePackageName,
                screenWidth = screenshot.width,
                screenHeight = screenshot.height,
                rootTree = tree,
                selections = selections,
            )
            container.captureRepository.save(detail)
            overlayManager.hideInspectOverlay()
            Toast.makeText(
                this@InspectorAccessibilityService,
                getString(R.string.toast_capture_saved),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}
