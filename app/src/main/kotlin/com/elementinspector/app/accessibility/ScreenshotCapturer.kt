package com.elementinspector.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Abstraction over screen capture so the service doesn't depend directly on the
 *  AccessibilityService.takeScreenshot API — useful for testing or for swapping
 *  in a different capture mechanism in the future. */
interface ScreenshotCapturer {
    suspend fun capture(): Bitmap?
}

/**
 * Uses the AccessibilityService#takeScreenshot API (API 30+), the only
 * non-root way for an accessibility service to capture the full screen.
 * The result arrives as a hardware bitmap, which is copied into a regular
 * ARGB_8888 bitmap so it can be drawn to a Canvas and saved as a PNG.
 */
class AccessibilityScreenshotCapturer(
    private val service: AccessibilityService,
) : ScreenshotCapturer {

    override suspend fun capture(): Bitmap? = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(service)
        service.takeScreenshot(
            Display.DEFAULT_DISPLAY,
            executor,
            object : AccessibilityService.TakeScreenshotCallback {
                override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                    val hardwareBitmap = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                    result.hardwareBuffer.close()
                    val softwareBitmap = hardwareBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                    hardwareBitmap?.recycle()
                    if (continuation.isActive) continuation.resume(softwareBitmap)
                }

                override fun onFailure(errorCode: Int) {
                    if (continuation.isActive) continuation.resume(null)
                }
            },
        )
    }
}
