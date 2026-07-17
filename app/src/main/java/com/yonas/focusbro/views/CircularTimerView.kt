package com.yonas.focusbro.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class CircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progress = 1f // 1 = full, 0 = empty
    private var animator: ValueAnimator? = null

    private var strokeWidth = 20f

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.parseColor("#34C759")

        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = strokeWidth
        bgPaint.color = Color.parseColor("#E5E5EA")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = (width / 2f) - strokeWidth

        canvas.drawCircle(cx, cy, radius, bgPaint)

        val startAngle = -90f
        val sweepAngle = 360f * progress
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, startAngle, sweepAngle, false, paint)
    }

    fun setProgress(value: Float, animate: Boolean = true) {
        val target = value.coerceIn(0f, 1f)
        if (animate) {
            animator?.cancel()
            val start = progress
            animator = ValueAnimator.ofFloat(start, target).apply {
                duration = 300
                interpolator = LinearInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            progress = target
            invalidate()
        }
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}