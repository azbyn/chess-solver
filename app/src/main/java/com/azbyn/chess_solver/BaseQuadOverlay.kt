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
    //private val roi get() = viewModel.roi
    /*var matWidth = 0
        private set
    var matHeight = 0
        private set
*/
    protected abstract val points: Array<Point>
    //protected fun po
    //lateinit var vm: SquareFragment.VM

    // =  vm.points
    private var pointIndex = -1
    private val initialDelta = Point()

    //protected val rect = RectF()
    //abstract val roi : CvRect//()

    //private val prev = PointF()
    private val p = PointF()
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    /*fun init(drawableWidth: Int, drawableHeight: Int, imageView: ZoomableImageView, from: SquareFragment) {
        init(drawableWidth, drawableHeight, imageView)
        vm = from.viewModel
    }*/
    final override fun init(drawableWidth: Int, drawableHeight: Int, imageView: ZoomableImageView) {
        super.init(drawableWidth, drawableHeight, imageView)
        //matWidth = drawableWidth
        //matHeight = drawableHeight
        pressRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 5f,
                resources.displayMetrics) //* matWidth / width / matWidth
        //logd("OIDA")
        imageView.resetZoom()
        //logd("margin: $margin, ${imageView.margin}")
        reset()
    }
    protected fun drawLine(canvas: Canvas, p1: Point, p2: Point, paint: Paint) {
        val a = imageView!!.drawableToScreenRet(p1.x.toFloat(), p1.y.toFloat())
        val b = imageView!!.drawableToScreenRet(p2.x.toFloat(), p2.y.toFloat())
        canvas.drawLine(a.x, a.y, b.x, b.y, paint)
    }

    @CallSuper
    override fun onDrawImpl(canvas: Canvas) {
//        fun drawLine(p1: Point, p2: Point) = drawLine(canvas, p1, p2, linePaint)
//
//        drawLine(points[0], points[1])
//        drawLine(points[1], points[3])
//        drawLine(points[2], points[3])
//        drawLine(points[2], points[0])

        for (i in 0 until 4) {
            val p0 = points[i]
            val p1 = points[(i +1) % 4]
            drawLine(canvas, p0, p1, linePaint)

        }


        /*for (i in 0..3) {
            drawLine(points[i], points[(i+1)%4])
        }*/
    }

    fun reset() { update() }

    final override fun onTouchImpl(event: MotionEvent): Boolean {
        if (pointIndex < 0 && event.action != MotionEvent.ACTION_DOWN)
            return false

        if (event.pointerCount > 1) {
            pointIndex = -1
            return false
        }
        //logd("oida %02X".format(pressType))

        p.x = event.x
        p.y = event.y
        this.imageView?.screenToDrawable(p)

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val closeEnough2 = pressRadius * pressRadius
                val scale2 = scale*scale
                for ((i, q) in points.withIndex()) {
                    //val p = points[0, 0]
                    val delta2 = dist2(p, q) * scale2
                    //val delta = sqrt(dist2(p, q)) * scale
                    //logi("scale = ${scale}")
                    //logi("sqrt(delta): ${sqrt(delta2)}; $delta")
                    if (delta2 < closeEnough2) {
                        //logi("close 'nuff")
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
                //logd("cancel/up")
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
    /*
    private fun onMove(x: Float, y: Float) {
        //logd("move")
        val deltaX = (x - prev.x).toInt()
        val deltaY = (y - prev.y).toInt()

        val isCenter = pressType == (PT_H_CENTER or PT_V_CENTER)

        if (isCenter) {
            roi.x += deltaX
            if (roi.x < 0) {
                roi.x = 0
            } else if (roi.x + roi.width > matWidth) {
                roi.x = matWidth - roi.width
            }

            roi.y += deltaY
            if (roi.y < 0) {
                roi.y = 0
            } else if ((roi.y + roi.height) > matHeight) {
                roi.y = matHeight - roi.height
            }
        } else {
            if ((pressType and PT_LEFT) != 0) {
                roi.x += deltaX
                roi.width -= deltaX
                if (roi.width < MIN_SIZE) {
                    val right = roi.x + roi.width
                    roi.x = right - MIN_SIZE
                    roi.width = MIN_SIZE
                } else if (roi.x < 0) {
                    roi.width += roi.x
                    roi.x = 0
                }
            }
            else if ((pressType and PT_RIGHT) != 0) {
                roi.width += deltaX
                if (roi.width < MIN_SIZE) {
                    roi.width = MIN_SIZE
                } else if (roi.x + roi.width > matWidth) {
                    roi.width = matWidth - roi.x
                }
            }

            if ((pressType and PT_TOP) != 0) {
                roi.y += deltaY
                roi.height -= deltaY
                if (roi.height < MIN_SIZE) {
                    val bot = roi.y + roi.height
                    roi.y = bot - MIN_SIZE
                    roi.height = MIN_SIZE
                } else if (roi.y < 0) {
                    roi.height += roi.y
                    roi.y = 0
                }
            }
            else if ((pressType and PT_BOTTOM) != 0) {
                roi.height += deltaY
                if (roi.height < MIN_SIZE) {
                    roi.height = MIN_SIZE
                } else if (roi.y + roi.height > matHeight) {
                    roi.height = matHeight - roi.y
                }
            }
        }
        update()
    }
     */

}
