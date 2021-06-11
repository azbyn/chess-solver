package com.azbyn.chess_solver.step2

import android.util.Log
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.Misc.logw
import com.azbyn.chess_solver.Misc.logwtf
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import java.lang.Exception
import kotlin.math.*
import org.opencv.core.Scalar

//ρ = x cosθ + y sinθ
data class HesseLine(val rho: Double, val theta: Double,
                val sinTh: Double = sin(theta),
                val cosTh: Double = cos(theta)) {
    //might fail
    fun xIntersection(x: Double): Double = (rho-x*cosTh) / sinTh
    // ditto
    fun yIntersection(y: Double): Double = (rho-y*sinTh) / cosTh


    //val isVertical get() = theta < PI/4 || theta > PI*3/4
    fun drawTo(mat: Mat, col: Scalar, thickness: Int = 3) {
        val x0 = cosTh * rho
        val y0 = sinTh * rho
        val big = 10000
        val p1 = Point(x0 + big * (-sinTh), y0 + big * cosTh)
        val p2 = Point(x0 - big * (-sinTh), y0 - big * cosTh)
        line(mat, p1, p2, col, thickness)
    }
    fun rotated(angleRad: Double, rotationMatrix: DoubleArray): HesseLine {
        //val oldTh = theta
        //val oldR = rho
        val theta = (this.theta - angleRad + PI) % (PI)
        //val theta = this.theta - angleRad
        @Suppress("UnnecessaryVariable") val m = rotationMatrix

        //val rho = r - sub
        //logi("theta = ${theta.toDeg()}")
        val cosTh = cos(theta)
        val sinTh = sin(theta)
        val oldX0 = this.cosTh * this.rho
        val oldY0 = this.sinTh * this.rho

        // rho = x cos(theta) + y sin(theta)

        val x0 = m[0] * oldX0 + m[1] *oldY0 + m[2]
        val y0 = m[3] * oldX0 + m[4] *oldY0 + m[5]

        //val x0 = m[0, 0][0] * oldX0 + m[0, 1][0] *oldY0 + m[0, 2][0]
        //val y0 = m[1, 0][0] * oldX0 + m[1, 1][0] *oldY0 + m[1, 2][0]
        val rho = cosTh * x0 + sinTh * y0
        return HesseLine(rho, theta, cosTh = cosTh, sinTh = sinTh)
    }
    fun intersection(l: HesseLine): Point {
        val a = cosTh
        val b = sinTh
        val c = rho

        val d = l.cosTh
        val e = l.sinTh
        val f = l.rho
        val div = a*e-b*d
        return Point(
            (c*e - b*f)/div,
            (a*f - c*d)/div)
    }
}

class LineMerge2Fragment : BaseSlidersFragment(
//    SliderData("thrsh", default = 100/*100*//*75*/, min = 0, max = 255*2, stepSize = 5),
//    SliderData("angle", default=5, min = 1, max = 30, stepSize =1)
    // for my board there's a margin of about one square
    SliderData("hasMargin", default=1, min=0, max=1, stepSize=1),
    SliderData("xOffset", default=0, min=-100, max=100, stepSize=2),
    SliderData("yOffset", default=0, min=-100, max=100, stepSize=2)


) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Merge 2"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
        private val inViewModel: Line2Fragment.VM by viewModelDelegate()
        private val ogMat get() = getViewModel<PerspectiveFragment.VM>().resultMat
        private var previewMat = Mat()
        private val horiLines get() = inViewModel.horiLines
        private val vertLines get() = inViewModel.vertLines

        var outHori = DoubleArray(9)
        var outVert = DoubleArray(9)


//        //super hacky
//        val rect get() = CvRect(outVert[0].toInt(), outHori[0].toInt(),
//            outVert.last().toInt(), outHori.last().toInt())



//        val goodLines = arrayListOf<Line>()
        //var resultMat = Mat()
        //    private set
//        var angleDeg = 0.0
//            private set
//        private var firstUpdate = true

//        override fun init(frag: BaseFragment) {
//            super.init(frag)
//            firstUpdate = true
//        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val hasMargin = args[0] != 0
            val xOffset = args[1]
            val yOffset = args[2]

            val squareSize = ogMat.width() / (if (hasMargin) 10 else 8)
            //val prevTh = lastValues[0]

            fun impl(lines: ArrayList<HesseLine>, extraOffset: Int,
                     getCoord: (x: HesseLine)->Double): DoubleArray {
//                val buckets = IntArray(8)
                val coords = ArrayList(lines.map(getCoord).sorted())// as MutableList

//                if (args[1] < lines.size) {
//                    val v = coords[args[1]]
//                    line(previewMat, Point(0.0, v), Point(ogMat.width().toDouble(), v),
//                        Colors.red, 3)
//                }
//
//                logd("ß: $squareSize")

                val eps = squareSize /4
                fun joinClose(startIdx: Int) {
//                    var endIdx = startIdx + 1
                    var cnt = 1

                    var sum = coords[startIdx] + coords[startIdx+1]
                    for (i in startIdx+2 until coords.size) {
                        val prev = coords[i-1]
                        val c = coords[i]
                        val delta = c - prev
//                        logd("[$i]: delta[$cnt]: $delta < $eps?")
                        if (delta > eps) {
                            break
                        }
                        sum += c
                        ++cnt
                    }

                    coords[startIdx] = sum / (cnt+1)

                    coords.removeCount(startIdx+1, cnt)
                }
                logd("things: ${coords.toStr()}")

                var i = 1
                while (i < coords.size) {
                    val prev = coords[i-1]
                    val c = coords[i]
                    val delta = c - prev
                    if (delta < eps) {
                        joinClose(i-1)
                    }
//                  //logd("Δ: $delta")
                    ++i
                }
//
                val averageLen: Double
                var averageOffset: Double

                if (coords.size > 2) {
                    var lenSum = 0.0
                    var cnt = 0
                    val res = arrayListOf<Double>(coords[0])
                    for (j in 1 until coords.size) {
                        val delta = coords[j] - coords[j - 1]
                        // todo if scale == 0
                        val scale = round(delta / squareSize).toInt()
                        if (scale > 1) {
                            for (k in 0 until scale) {
                                res.add(coords[j] + k*(delta/scale))
                            }
                        } else {
                            res.add(coords[j])
                        }
                        lenSum += delta
                        cnt += scale
                    }
                    averageLen = lenSum / cnt
                    //if values are near 0 it would be bad if we just summed (it % averageLen)
                    //we already implemented a function that does the circular average of values
                    //in 0..pi, so let's use that.
                    averageOffset = coords.map { (it % averageLen)/averageLen * PI/2 }
                        .angleAverage() / (PI/2) * averageLen
//                    return res.toDoubleArray()
//                    return ((0..8).map { it * averageLen + averageOffset }).toDoubleArray()
                } else {
                    logw("Not enough lines found")
                    averageLen = squareSize.toDouble()
                    averageOffset = 0.0
                }

//                logd("offset: $averageOffset, len $averageLen  ź ${coords.map{it%averageLen}.toStr()}")
                val remainder =
                    /*round*/((ogMat.height() - (averageOffset + averageLen * 8)) / averageLen / 2)
                val remainder2 = averageOffset / averageLen /2
                logd("remainder = ${remainder.format()}, r2 = ${remainder2.format()}")
                averageOffset += round(remainder -remainder2) * averageLen + extraOffset

                return ((0..8).map { it * averageLen + averageOffset }).toDoubleArray()
            }

            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            val middle = ogMat.width()/2.0
//            outHori = impl(horiLines) {
//                val v = it.xIntersection(middle)
//                line(previewMat, Point(0.0, v), Point(ogMat.width().toDouble(), v),
//                    Colors.green, 3)
//                v
//            }
            outHori = impl(horiLines, yOffset) { it.xIntersection(middle) }
            outVert = impl(vertLines, xOffset) { it.yIntersection(middle) }

//            outVert = impl(vertLines) {
//                it.yIntersection(middle).also {v ->
//                    line(previewMat, Point(v, 0.0), Point(v, ogMat.height().toDouble()),
//                        Colors.green, 3)
//                }
//            }
            val color = Colors.darkGreen
            for (c in outHori) {
                line(previewMat, Point(0.0, c), Point(ogMat.width().toDouble(), c),
                    color, 3)
            }
            for (c in outVert) {
                line(previewMat, Point(c, 0.0), Point(c, ogMat.height().toDouble()),
                    color, 3)
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(previewMat)// setImageGrayscalePreview(resultMat)
            }
        }
    }
}
