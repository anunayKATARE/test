package com.elementinspector.app.accessibility.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.elementinspector.domain.model.ElementNode
import com.elementinspector.domain.model.ElementSelection
import com.elementinspector.domain.resolver.ElementAtPointResolver

/**
 * Renders a frozen screenshot scaled to the view's width and lets the user
 * tap it to inspect accessibility elements. The screenshot and the
 * accessibility tree share the same pixel coordinate space, so both drawing
 * and tap coordinates are scaled by width/screenshot.width to convert
 * between view space and that shared bitmap space.
 */
class InspectOverlayView(
    context: Context,
    private val screenshot: Bitmap,
    private val tree: ElementNode,
    private val resolver: ElementAtPointResolver,
    initialSelections: List<ElementSelection> = emptyList(),
    private val onSelectionsChanged: (selections: List<ElementSelection>, focused: ElementNode?) -> Unit,
) : View(context) {

    private val selections = initialSelections.toMutableList()
    private var focusedElement: ElementNode? = null

    private val highlightPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#FF4081")
    }
    private val focusedPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#FFC107")
    }
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#334081FF")
    }

    init {
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = if (screenshot.width > 0) {
            (measuredWidth.toLong() * screenshot.height / screenshot.width).toInt()
        } else {
            0
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (screenshot.width == 0 || width == 0) return
        val scale = width.toFloat() / screenshot.width

        canvas.save()
        canvas.scale(scale, scale)
        canvas.drawBitmap(screenshot, 0f, 0f, null)
        for (selection in selections) {
            val isFocused = selection.element === focusedElement
            val bounds = selection.element.bounds
            val rect = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
            canvas.drawRect(rect, fillPaint)
            canvas.drawRect(rect, if (isFocused) focusedPaint else highlightPaint)
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && screenshot.width > 0 && width > 0) {
            val scale = width.toFloat() / screenshot.width
            handleTap((event.x / scale).toInt(), (event.y / scale).toInt())
        }
        return true
    }

    private fun handleTap(x: Int, y: Int) {
        val resolved = resolver.resolve(tree, x, y) ?: return
        val existingIndex = selections.indexOfFirst {
            it.element.bounds == resolved.bounds && it.element.viewIdResourceName == resolved.viewIdResourceName
        }
        if (existingIndex >= 0) {
            selections.removeAt(existingIndex)
            focusedElement = null
        } else {
            selections.add(ElementSelection(tapX = x, tapY = y, element = resolved))
            focusedElement = resolved
        }
        invalidate()
        onSelectionsChanged(selections.toList(), focusedElement)
    }

    fun currentSelections(): List<ElementSelection> = selections.toList()
}
