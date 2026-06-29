package com.elementinspector.app.ui.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.elementinspector.domain.model.RectDto

/** Renders a saved screenshot scaled to the view's width, with the saved
 *  selection bounds drawn on top in the same coordinate space as the bitmap. */
class ScreenshotHighlightView(context: Context) : View(context) {

    private var bitmap: Bitmap? = null
    private var highlights: List<RectDto> = emptyList()

    private val highlightPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#FF4081")
        isAntiAlias = true
    }

    fun setData(bitmap: Bitmap, highlights: List<RectDto>) {
        this.bitmap = bitmap
        this.highlights = highlights
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val bmp = bitmap
        val height = if (bmp != null && bmp.width > 0) (width.toLong() * bmp.height / bmp.width).toInt() else 0
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return
        if (bmp.width == 0 || width == 0) return
        val scale = width.toFloat() / bmp.width

        canvas.save()
        canvas.scale(scale, scale)
        canvas.drawBitmap(bmp, 0f, 0f, null)
        for (rect in highlights) {
            canvas.drawRect(Rect(rect.left, rect.top, rect.right, rect.bottom), highlightPaint)
        }
        canvas.restore()
    }
}
