package com.sharmadipanshu.aistudybuddy.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sharmadipanshu.aistudybuddy.models.HighlightArea

class HighlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        isClickable = false
        isLongClickable = false
        isFocusable = false
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FACC15")
        style = Paint.Style.FILL
    }

    private var highlightArea: HighlightArea? = null

    fun updateHighlight(area: HighlightArea?) {
        highlightArea = area
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val area = highlightArea ?: return
        canvas.drawRoundRect(
            area.x,
            area.y,
            area.x + area.width,
            area.y + area.height,
            24f,
            24f,
            highlightPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = false
}
