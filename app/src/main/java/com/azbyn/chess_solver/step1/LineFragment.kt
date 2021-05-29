package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logw
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

//ρ = x cosθ + y sinθ
data class Line(val rho: Double, val theta: Double,
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
    fun rotated(angleRad: Double, rotationMatrix: DoubleArray): Line {
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
        return Line(rho, theta, cosTh = cosTh, sinTh = sinTh)
    }
    fun intersection(l: Line): Point {
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

class LineFragment : BaseSlidersFragment(
    SliderData("thrsh", default = 135/*100*//*75*/, min = 0, max = 255*2, stepSize = 5),
    SliderData("buckets", default=30, min = 4, max = 60, stepSize =2),
    SliderData("close", default=1, min=0, max=5, stepSize=1)
) {
    //TODO FIND BIGGEST SQUARE or partial square?
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Line"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
        private val inViewModel: EdgeFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()
        //var resultMat = Mat()
        //    private set
        val goodLines = arrayListOf<Line>()
        var angleDeg = 0.0
            private set
        private var firstUpdate = true

        override fun init(frag: BaseFragment) {
            super.init(frag)
            firstUpdate = true
        }

        override fun update(p: IntArray, isFastForward: Boolean) {
            //val prevTh = lastValues[0]
            super.update(p, isFastForward)
            val lines = Mat()

            val th = p[0]
            val bucketCount = p[1]
            val closeNuff = p[2]
            //if (firstUpdate || th != prevTh) {
            //    firstUpdate = false
                //logd("OIDA")
                HoughLines(baseMat, lines, 1.0, PI / 180.0, th)
            //}
            if (lines.rows() > 5000) {
                logw("Ty debil")
                p[0] += 50
                update(p, isFastForward)
                return
            } else if (lines.rows() > 500) {
                p[0] += 20
                update(p, isFastForward)
                return
            } else if (lines.rows() > 200) {
                p[0] += 10
                update(p, isFastForward)
                return
            } else if (lines.rows() < 20) {
                p[0] -= 10
            }
        //ogMat.copyTo(resultMat)
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
            //val buf = DoubleArray(2)
            //lines.reshape(lines.rows()/2)
            // logd("${lines.size()} : ${lines.rows()}")
//            class Bucket(var hori: Int, var vert: Int) {
//                val total get() = hori+vert
//            }
            //val buckets = Array(bucketCount){Bucket(0,0)} //IntArray(bucketCount)
            val buckets = IntArray(bucketCount)


            for (i in 0 until lines.rows() step 2) {
                val buf = lines.get(i,0)
                val theta = buf[1]
                val bukIndex = ((theta * bucketCount) / PI).toInt()
                ++buckets[bukIndex]
            }

            val half = bucketCount / 2
            fun modDist(a: Int, b: Int): Int {
                val dif = abs(b - a)
                return if (dif > half) /*abs*/(bucketCount - dif) else dif
            }
            fun getOtherIdx(ind: Int) = if (ind < half) (ind + half) else (ind - half)

            //logi("buckets: ${buckets.toStr()}")
            var ind = buckets.indices.maxByOrNull { buckets[it] }!!
            var ind2 = getOtherIdx(ind)
            val backups = Pair(ind, ind2)
            while (true) {
                //fun getIndx(backup: Int): Int {

                if (buckets[ind2] < 2) {// * 8 < buckets[ind]) {
                    //prevent infinite loop
                    if (buckets[ind] == 0) {
                        ind = backups.first
                        ind2 = backups.second
                        break
                    }

                    buckets[ind] = 0
                    buckets[ind2] = 0
                } else {
                    break
                }
                ind = buckets.indices.maxByOrNull { buckets[it] }!!
                // if buckets[ind2] < buckets[ind]/2 goto the second one
                // aka if we don't have perpendicular lines
                ind2 = getOtherIdx(ind)
            }
            val indx = min(ind, ind2)
                //return indx
            //}

            //ogi("idx = $indx =  min($ind, $ind2)")
            this.angleDeg = (indx * 180.0) / bucketCount

            goodLines.clear()
            for (i in 0 until lines.rows() step 2) {
                val buf = lines.get(i, 0)
                val line = Line(rho=buf[0], theta=buf[1])
                val bukIndex = ((line.theta * bucketCount) / PI).toInt()
                val col = when {
                    bukIndex == ind || bukIndex == ind2 -> {
                        goodLines.add(line)
                        Colors.green
                    }
                    modDist(bukIndex, ind) <= closeNuff ||
                    modDist(bukIndex, ind2) <= closeNuff -> {
                        goodLines.add(line)
                        Colors.blue
                    }
                    else -> Colors.red
                }
                line.drawTo(previewMat, col)
            }
            logi("length = ${goodLines.size} / ${lines.height()}")
//            if (goodLines.size > lines.height()/2) {
//                logi("too many wrong")
//                p[0] += 10
//                update(p, isFastForward)
//            }'
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(previewMat)// setImageGrayscalePreview(resultMat)
            }
        }
    }
}
