package com.elementinspector.app.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import androidx.core.widget.NestedScrollView

/**
 * A [NestedScrollView] capped to half the display height. When placed inside
 * an outer scroll container it cooperates through the standard nested-scroll
 * protocol: it scrolls internally until it hits its own top/bottom, then the
 * gesture falls through to the outer container — so a section only grows up
 * to half the screen and becomes independently scrollable past that.
 */
class HalfScreenScrollSection @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : NestedScrollView(context, attrs) {

    private val maxHeightPx: Int by lazy {
        val windowManager = context.getSystemService(WindowManager::class.java)
        windowManager.currentWindowMetrics.bounds.height() / 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cappedHeightSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, cappedHeightSpec)
    }
}
