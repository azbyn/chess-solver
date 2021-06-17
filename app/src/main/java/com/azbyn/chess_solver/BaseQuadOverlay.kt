package com.azbyn.chess_solver

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.annotation.CallSuper
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

abstract class BaseQuadOverlay : BaseOverlay {
    private var pressRadius = 0f
    private val scale get() = imageView?.matrixScale ?: 1f

    @Suppress("MemberVisibilityCanBePrivate")
    protected val linePaint = Paint().apply {
        strokeWidth = 5f
        color = 0xFF00FF00.toInt()
    }
    protected abstract val points: Array<Point>

    private var pointIndex = -1
    private val initialDelta = Point()

    private val p = PointF()
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    final override fun init(drawableWidth: Int, drawableHeight: Int, imageView: ZoomableImageView) {
        super.init(drawableWidth, drawableHeight, imageView)
        pressRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 5f,
                resources.displayMetrics) //* matWidth / width / matWidth

        imageView.resetZoom()

        reset()
    }
    protected fun drawLine(canvas: Canvas, p1: Point, p2: Point, paint: Paint) {
        val a = imageView!!.drawableToScreenRet(p1.x.toFloat(), p1.y.toFloat())
        val b = imageView!!.drawableToScreenRet(p2.x.toFloat(), p2.y.toFloat())
        canvas.drawLine(a.x, a.y, b.x, b.y, paint)
    }

    @CallSuper
    override fun onDrawImpl(canvas: Canvas) {
        for (i in 0 until 4) {
            val p0 = points[i]
            val p1 = points[(i +1) % 4]
            drawLine(canvas, p0, p1, linePaint)
        }
    }

    fun reset() { update() }

    final override fun onTouchImpl(event: MotionEvent): Boolean {
        if (pointIndex < 0 && event.action != MotionEvent.ACTION_DOWN)
            return false

        if (event.pointerCount > 1) {
            pointIndex = -1
            return false
        }

        p.x = event.x
        p.y = event.y
        this.imageView?.screenToDrawable(p)

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val closeEnough2 = pressRadius * pressRadius
                val scale2 = scale*scale
                for ((i, q) in points.withIndex()) {

                    val delta2 = dist2(p, q) * scale2

                    if (delta2 < closeEnough2) {
                        this.pointIndex = i
                        this.initialDelta.x = p.x-q.x
                        this.initialDelta.y = p.y-q.y

                        return true
                    }
                }
                false
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                pointIndex = -1
                true
            }
            MotionEvent.ACTION_MOVE -> {
                points[pointIndex].x = p.x - initialDelta.x
                points[pointIndex].y = p.y - initialDelta.y
                update()

                true
            }
            else -> false
        }
    }
}
