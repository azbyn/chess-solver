package com.azbyn.chess_solver.step2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.azbyn.chess_solver.BaseRoiOverlay
import com.azbyn.chess_solver.CvRect
import com.azbyn.chess_solver.Misc.logi
import com.azbyn.chess_solver.ZoomableImageView

class SmallSquaresOverlay : BaseRoiOverlay {
    lateinit var vm: SmallSquaresFragment.VM

    private val mainPaint = Paint().apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
        color = 0xFF00FF00.toInt()
    }
    private val interLinePaint = Paint().apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        color = 0xFF00FFFF.toInt()
    }

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    fun init(drawableWidth: Int, drawableHeight: Int, imageView: ZoomableImageView, from: SmallSquaresFragment) {
        vm = from.viewModel
        init(drawableWidth, drawableHeight, imageView)
    }

    override val roi: CvRect get() = vm.rect

    private val screenRect = RectF()

    override fun onDrawImpl(canvas: Canvas) {
        imageView?.mapRect(rect, screenRect)
        canvas.drawRect(screenRect, mainPaint)
        val numSquares = 8
        val xIncrement = screenRect.width() / numSquares
        val yIncrement = screenRect.height() / numSquares

        for (i in 1 until numSquares) {
            val y = screenRect.top + i * yIncrement
            canvas.drawLine(screenRect.left, y, screenRect.right,y, interLinePaint)
        }

        for (i in 1 until numSquares) {
            val x = screenRect.left + i * xIncrement
            canvas.drawLine(x, screenRect.top, x, screenRect.bottom, interLinePaint)
        }
    }
}
