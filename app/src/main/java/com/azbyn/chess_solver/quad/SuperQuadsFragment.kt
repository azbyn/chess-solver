package com.azbyn.chess_solver.quad

import com.azbyn.chess_solver.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

class SuperQuadsFragment : BaseSlidersFragment(
    SliderData("centerEps", default = 10, min = 5, max = 200, stepSize = 5),
    SliderData("epsPerc", default = 25, min = 5, max = 100, stepSize = 5),
    SliderData("angle", default = 15, min = 1, max = 45, stepSize = 1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Super Quads"

    class VM : SlidersViewModel() {
        private val inViewModel: ContoursFragmentOld.VM by viewModelDelegate()
        private val quads get() = inViewModel.quads
        private var previewMat = Mat()
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        val grids = arrayListOf<Grid>()

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val centerEps = p[0]
            val centerEps2 = centerEps.toDouble()* centerEps
            val epsPerc = p[1].toDouble() / 100
            val eps2 = epsPerc * inViewModel.avrgArea
            val eps = sqrt(eps2).toInt()
            val angleCloseNuff = p[2].toRad()


            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
            logi("quad-len = ${quads.size}")
            grids.clear()

            fun addToGrids(q: PointQuad) {
                q.sortCW(angleCloseNuff)
                for (g in grids) {
                    if (g.tryAdd(q, centerEps2, eps2)) return
                }
                grids.add(Grid(q))
            }
            for (q in quads) addToGrids(q)
            logi("grid $grids - ${grids.size}")

            val colors = arrayOf( Colors.darkBlue, Colors.darkGreen, Colors.darkRed, Colors.darkCyan,
                Colors.magenta, Colors.yellow, Colors.gray, Colors.white)
            for ((j, g) in grids.withIndex()) {
                //logi("g = ${g.quads.size}")
                val c = colors[j % colors.size]
                g.drawTo(previewMat, c, eps)
                /*
            for (q in sq.quads) {
                q.drawTo(previewMat, c, eps)
                val colors2 = arrayOf(Colors.blue, Colors.green, Colors.red, Colors.cyan)
                for (i in 0 until 4) {
                    circle(previewMat, q[i], 1, colors2[i], eps)
                }
                circle(previewMat, q.center, 1, c, centerEps)
            }*/
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
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                update(p)
                frag.setImagePreview(previewMat)
            }
        }
    }
}
