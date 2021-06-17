package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logw
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*
import org.opencv.core.Scalar

//ρ = x cosθ + y sinθ
data class HesseLine(val rho: Double, val theta: Double,
                val sinTh: Double = sin(theta),
                val cosTh: Double = cos(theta)) {
    // might fail
    fun xIntersection(x: Double): Double = (rho-x*cosTh) / sinTh
    // might fail
    fun yIntersection(y: Double): Double = (rho-y*sinTh) / cosTh

    fun drawTo(mat: Mat, col: Scalar, thickness: Int = 3) {
        val x0 = cosTh * rho
        val y0 = sinTh * rho
        val big = 10000
        val p1 = Point(x0 + big * (-sinTh), y0 + big * cosTh)
        val p2 = Point(x0 - big * (-sinTh), y0 - big * cosTh)
        line(mat, p1, p2, col, thickness)
    }
}

class LineMerge2Fragment : BaseSlidersFragment(
    // for our board there's a margin of about one square
    SliderData("hasMargin", default=1, min=0, max=1, stepSize=1),
    SliderData("xOffset", default=0, min=-100, max=100, stepSize=2),
    SliderData("yOffset", default=0, min=-100, max=100, stepSize=2)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Merge 2"

    class VM : SlidersViewModel() {
        private val inViewModel: Line2Fragment.VM by viewModelDelegate()
        private val origMat get() = getViewModel<PerspectiveFragment.VM>().resultMat
        private var previewMat = Mat()
        private val horiLines get() = inViewModel.horiLines
        private val vertLines get() = inViewModel.vertLines

        var outHori = DoubleArray(9)
        var outVert = DoubleArray(9)

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val hasMargin = args[0] != 0
            val xOffset = args[1]
            val yOffset = args[2]

            val squareSize = origMat.width() / (if (hasMargin) 10 else 8)

            fun impl(lines: ArrayList<HesseLine>, extraOffset: Int,
                     getCoord: (x: HesseLine)->Double): DoubleArray {
                val coords = ArrayList(lines.map(getCoord).sorted())

                val eps = squareSize / 4
                fun joinClose(startIdx: Int) {
                    var cnt = 1

                    var sum = coords[startIdx] + coords[startIdx+1]
                    for (i in startIdx+2 until coords.size) {
                        val prev = coords[i-1]
                        val c = coords[i]
                        val delta = c - prev

                        if (delta > eps) {
                            break
                        }
                        sum += c
                        ++cnt
                    }

                    coords[startIdx] = sum / (cnt+1)

                    coords.removeCount(startIdx+1, cnt)
                }

                var i = 1
                while (i < coords.size) {
                    val prev = coords[i-1]
                    val c = coords[i]
                    val delta = c - prev
                    if (delta < eps) {
                        joinClose(i-1)
                    }

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
                } else {
                    logw("Not enough lines found")
                    averageLen = squareSize.toDouble()
                    averageOffset = 0.0
                }


                val remainder =
                    /*round*/((origMat.height() - (averageOffset + averageLen * 8)) / averageLen / 2)
                val remainder2 = averageOffset / averageLen /2
                logd("remainder = ${remainder.format()}, r2 = ${remainder2.format()}")
                averageOffset += round(remainder -remainder2) * averageLen + extraOffset

                return ((0..8).map { it * averageLen + averageOffset }).toDoubleArray()
            }

            cvtColor(origMat, previewMat, COLOR_GRAY2RGB)

            val middle = origMat.width()/2.0

            outHori = impl(horiLines, yOffset) { it.xIntersection(middle) }
            outVert = impl(vertLines, xOffset) { it.yIntersection(middle) }

            val color = Colors.darkGreen
            for (c in outHori) {
                line(previewMat, Point(0.0, c), Point(origMat.width().toDouble(), c),
                    color, 3)
            }
            for (c in outVert) {
                line(previewMat, Point(c, 0.0), Point(c, origMat.height().toDouble()),
                    color, 3)
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(previewMat)
            }
        }
    }
}
