package com.azbyn.chess_solver.quad

import com.azbyn.chess_solver.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

class FinalizeGridFragment : BaseSlidersFragment(
    SliderData("centerEps", default = 10, min = 5, max = 200, stepSize = 5),
    SliderData("epsPerc", default = 25, min = 0, max = 100, stepSize = 1),
    SliderData("i", default = -1, min = -1, max = 100, stepSize = 1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Finalize grid"

    class VM : SlidersViewModel() {
        private val inViewModel: SuperQuadsMergeFragment.VM by viewModelDelegate()
        private val grids get() = inViewModel.grids
        private var previewMat = Mat()
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val sums = DoubleArray(grids.size)
            var avrg = 0.0
            for ((i, g) in grids.withIndex()) {
                sums[i] = g.getAverageLineSize()
                avrg += sums[i]
                logi("grid[$i].size = ${g.getCount()}")
            }
            avrg /= grids.size
            logi("sums: (avrg $avrg) ${sums.toStr()}")
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            val index = p[2]

            val colors = arrayOf(Colors.darkBlue, Colors.darkGreen, Colors.darkRed, Colors.darkCyan,
                Colors.magenta, Colors.yellow, Colors.gray, Colors.white )
            if (index in grids.indices) {
                val c = colors[index % colors.size]
                grids[index].drawTo(previewMat, c, 3)
            } else {
                for ((j, g) in grids.withIndex()) {
                    //logi("g = ${g.quads.size}")
                    val c = colors[j % colors.size]
                    g.drawTo(previewMat, c, 3)
                }
            }
            /*
            val centerEps = p[0]
            val centerEps2 = centerEps.toDouble()* centerEps
            val epsPerc = p[1].toDouble() / 100
            val eps2 = epsPerc * getViewModel<ContoursFragment.VM>().avrgArea
            //val eps = max(sqrt(eps2).toInt(), 1)


            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            for ((i, sq1) in grids.withIndex()) {
                for ((j, sq2) in grids.withIndex()) {
                    if (i != j) {
                        if (sq1.tryMerge(sq2, centerEps2, eps2)) {
                            sq2.clear()
                            continue
                        }
                    }
                }
            }

            grids.removeAll { it.isEmpty() }
            logi("grids $grids")

            val colors = arrayOf(
                Colors.darkBlue,
                Colors.darkGreen,
                Colors.darkRed,
                Colors.darkCyan,
                Colors.magenta,
                Colors.yellow,
                Colors.gray,
                Colors.white
            )
            if (index in grids.indices) {
                val c = colors[index % colors.size]
                grids[index].drawTo(previewMat, c, 3)
            } else {
                for ((j, g) in grids.withIndex()) {
                    //logi("g = ${g.quads.size}")
                    val c = colors[j % colors.size]
                    g.drawTo(previewMat, c, 3)
                }
            }
            /*
            val centerEps = p[0]
            val centerEps2 = centerEps.toDouble()* centerEps
            val epsPerc = p[1].toDouble() / 100
            val eps2 = epsPerc * getViewModel<ContoursFragment.VM>().avrgArea
            val eps = max(sqrt(eps2).toInt(), 1)


            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
            for ((i, sq1) in superQuads.withIndex()) {
                for ((j, sq2) in superQuads.withIndex()) {
                    if (i != j) {
                        if (sq1.tryMerge(sq2, centerEps2, eps2)) {
                            continue
                        }
                    }
                }
            }

            superQuads.removeAll { it.quads.size == 0 }
            logi("über-quads $superQuads")

            val colors = arrayOf(Colors.darkBlue, Colors.darkGreen, Colors.darkRed, Colors.darkCyan,
                Colors.magenta, Colors.yellow, Colors.gray, Colors.white)

            for ((j, sq) in superQuads.withIndex()) {
                val c = colors[j % colors.size]
                for (q in sq.quads) {
                    q.drawTo(previewMat, c, eps)
                    val colors2 = arrayOf(Colors.blue, Colors.green, Colors.red, Colors.cyan)
                    for (i in 0 until 4) {
                        circle(previewMat, q[i], 1, colors2[i], eps)
                    }
                    circle(previewMat, q.center, 1, c, centerEps)
                }
                sq.getBounds().drawTo(previewMat, Colors.red, 5)
            }

            /*
            var angleSum = 0.0
            for (q in quads) {
                var minAngle = PI
                for (i in 0 until 4) {
                    val p0 = q[i]
                    val p1 = q[(i +1)% 4]
                    val dx = abs(p0.x - p1.x ) //if (p0.y < p1.y) (p1.x - p0.x) else (p0.x - p1.x)
                    val dy = abs(p1.y - p0.y)
                    val a = atan2(dy, dx) //range [0, pi]
                    if (a < minAngle) minAngle = a
                }
                logi("ma: ${minAngle / PI * 180}")
                angleSum += minAngle
            }
            var avrgAngle = (angleSum / quads.size) / PI * 180
            logi("avrgAngle $avrgAngle")
            if (invert) avrgAngle *= -1
            val w = ogMat.width().toDouble()
            val h = ogMat.height().toDouble()

            val m = getRotationMatrix2D(Point(w / 2, h / 2), avrgAngle, 1.0)
            warpAffine(ogMat, resultMat, m, resultMat.size()) // Size(w, h))
            cvtColor(resultMat, previewMat, COLOR_GRAY2RGB)
             */

             */

             */
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(previewMat)
            }
        }
    }
}
