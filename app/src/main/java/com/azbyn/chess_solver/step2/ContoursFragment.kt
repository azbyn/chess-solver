package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc.*
import kotlin.math.max


class ContoursFragment : BaseSlidersFragment(
    SliderData("min ‰", default = 50/*2000*/, min = 10, max = 250, stepSize = 5),
    SliderData("max ‰", default = 160, min = 10, max = 250, stepSize = 5),
    SliderData("MaxRatio",default = 60, min = 15, max = 150, stepSize = 5, showFloat=true)

    //SliderData("angle", default = 10, min = 1, max = 45, stepSize = 1),
    //SliderData("areaPerc", default = 5, min = 1, max = 10, stepSize = 1, showFloat = true)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Contours"

    class VM : SlidersViewModel() {
//        private val inViewModel: BlurFragment.VM by viewModelDelegate()
        private val inViewModel: DilateFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        private var previewMat = Mat()
        private val ogMat get() = getViewModel<PerspectiveFragment.VM>().resultMat

        val contours = arrayListOf<MatOfPoint>()

        //val quads = arrayListOf<PointQuad>()
        //var avrgArea = 0.0
        //    private set

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val minLen = p[0] * baseMat.width() / 1000
            val maxLen = p[1] * baseMat.width() / 1000
            val acceptableUpperRatio = p[2] * 0.1

            val minArea = minLen * minLen
            val maxArea = maxLen * maxLen

            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            /*
            val minArea = p[0]
            val parallelEnough = p[1] * PI / 180
            val areaThresholdPerc = p[2] * .1

            //ogMat.copyTo(resultMat)

            //val t2 = p[1].toDouble()
*/
            val tmpContours = arrayListOf<MatOfPoint>()
            val hierarchy = Mat()
            findContours(baseMat, tmpContours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE)

            val thickness = 3
            contours.clear()
            contours.ensureCapacity(tmpContours.count()/2)
            for ((i, c) in tmpContours.withIndex()) {
                val area = contourArea(c)
                //logi("area: $area")
                val col = when {
                    area < minArea -> Colors.blue
                    area > maxArea -> Colors.red
                    else -> {
                        val minMax = MinMaxRect()
                        minMax.checkContour(c)
                        val width  = minMax.x.delta
                        val height = minMax.y.delta
                        val ratio = width / height
                        val delimitedArea = width *height
                        if (ratio >= 1/acceptableUpperRatio && ratio <= acceptableUpperRatio &&
                            delimitedArea < maxArea*4)
                        {
                            contours.add(c)
                            Colors.green
                        } else {
                            Colors.magenta
                        }

                    }
                }
                drawContours(previewMat, tmpContours, i, col, thickness)// listOf(approx), 0, blue, thickness)
            }
            /*
            val approx2f = MatOfPoint2f()
            val approx = MatOfPoint()
            val curr2f = MatOfPoint2f()
            val sides = DoubleArray(4)
            val angles = DoubleArray(4)
            val thickness = 3
            var areaSums = 0.0
            quads.clear()
            for (c in contours) {
                c.convertTo(curr2f, CV_32FC2)
                //MatOfPoint2f(*c.toArray())
                val epsilon = 0.1 * arcLength(curr2f, true)
                approxPolyDP(curr2f, approx2f, epsilon, true)
                //logi("sz: ${approx.elemSize()}; ${approx.size()}")
                approx2f.convertTo(approx, CV_32S)
                val area = abs(contourArea(approx))
                if (approx.height() == 4 && area > minArea && isContourConvex(approx)) {
                    for (i in 0 until 4) {
                        val p0 = approx2f[i, 0]
                        val p1 = approx2f[(i +1)% 4, 0]
                        val dx = abs(p1[0] - p0[0])
                        val dy = abs(p1[1] - p0[1])
                        sides[i] = dx*dx + dy*dy
                        angles[i] = atan2(dy, dx)// in range [0, pi/2]
                        //line(previewMat, Point(p0[0], p0[1]), Point(p1[0], p1[1]), green, thickness)
                    }
                    sort(angles)
                    //drawContours(previewMat, listOf(approx), 0, blue, thickness)
                    if ((angles[1] - angles[1] < parallelEnough) &&
                        (angles[3] - angles[2] < parallelEnough)) {
                        quads.add(PointQuad(approx2f))
                        areaSums += area
                        //drawContours(previewMat, listOf(approx), 0, Colors.blue, thickness)
                    } else {
                        drawContours(previewMat, listOf(approx), 0,
                            Colors.magenta, thickness)
                        //drawContours(previewMat, listOf(approx), 0, red, thickness)
                    }

                } else if (area > 100) {
                    drawContours(previewMat, listOf(approx), 0,
                        Colors.red, 2)
                }
            }
            avrgArea = areaSums / quads.size
            //logi("aa: $avrgArea, ${quads.size}")
            val areaThreshold = avrgArea * areaThresholdPerc
            for (q in quads) {
                val col = if (q.area() >= areaThreshold) Colors.green else Colors.blue
                q.drawTo(previewMat, col, thickness=thickness)
            }
            quads.removeAll { it.area() < areaThreshold }
            //for (q in quads) q.sortCW()
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
