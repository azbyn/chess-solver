package com.azbyn.chess_solver.crop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.azbyn.chess_solver.BaseRoiOverlay
import com.azbyn.chess_solver.CvRect
import com.azbyn.chess_solver.GRAYED_OUT_COLOR

class CropOverlay : BaseRoiOverlay {
    override val roi = CvRect()
    private val density = resources.displayMetrics.density

    private val grayPaint: Paint = Paint().apply {
        strokeWidth = 0f
        color = GRAYED_OUT_COLOR
    }
    private val linePaint: Paint = Paint().apply {
        strokeWidth = density
        style = Paint.Style.STROKE
        //color = 0xA0_39_71_ED.toInt()
        color = 0xA0_FF_FF_FF.toInt()
    }

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    override fun reset() {
        roi.x = 0
        roi.y = 0
        roi.width = matWidth
        roi.height = matHeight
        super.reset()
    }

    private val screenRect = RectF()

    override fun onDrawImpl(canvas: Canvas) {
        imageView?.mapRect(rect, screenRect)
        linePaint.strokeWidth = 2f * density * (imageView?.currentZoom?:1f)
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawRect(0f,0f, screenRect.left, h, grayPaint)
        canvas.drawRect(screenRect.right,0f, w, h, grayPaint)
        canvas.drawRect(screenRect.left,0f, screenRect.right, screenRect.top, grayPaint)
        canvas.drawRect(screenRect.left, screenRect.bottom, screenRect.right, h, grayPaint)

        canvas.drawRect(screenRect, linePaint)
    }
}
