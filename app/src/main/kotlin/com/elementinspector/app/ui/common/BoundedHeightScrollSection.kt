package com.elementinspector.app.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import androidx.core.widget.NestedScrollView
import com.elementinspector.app.R

/**
 * A [NestedScrollView] capped to a fraction of the display height (defaults
 * to half via `app:heightFraction`). When placed inside an outer scroll
 * container it cooperates through the standard nested-scroll protocol: it
 * scrolls internally until it hits its own top/bottom, then the gesture
 * falls through to the outer container — so a section only grows up to its
 * cap and becomes independently scrollable past that.
 */
class BoundedHeightScrollSection @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : NestedScrollView(context, attrs) {

    private val heightFraction: Float = run {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoundedHeightScrollSection)
        try {
            typedArray.getFloat(R.styleable.BoundedHeightScrollSection_heightFraction, DEFAULT_HEIGHT_FRACTION)
        } finally {
            typedArray.recycle()
        }
    }

    private val maxHeightPx: Int by lazy {
        val windowManager = context.getSystemService(WindowManager::class.java)
        (windowManager.currentWindowMetrics.bounds.height() * heightFraction).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cappedHeightSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, cappedHeightSpec)
    }

    private companion object {
        const val DEFAULT_HEIGHT_FRACTION = 0.5f
    }
}
