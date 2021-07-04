package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logw
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class ConnectSegmentsFragment : BaseSlidersFragment(
    SliderData("radius", default=AcceptFragment.VM.convertLength(50), min = 5, max = 200, stepSize =5),
    SliderData("angle δ", default=20, min=5, max=90, stepSize=5),
//    SliderData("spacing", default=10, min = 5, max = 100, stepSize =5)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Connect Segments"

    class VM : SlidersViewModel() {
        private val inViewModel: ProbabilisticLineMergeFragment.VM by viewModelDelegate()
        private val inSegments get() = inViewModel.segments
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()

        private var remainingIters = 0

        override fun init(frag: BaseFragment) {
            super.init(frag)
            remainingIters = 10
        }

        fun redoRadius(radiusIncrease: Int, isFastForward: Boolean=true) {
            lastValues[0] += radiusIncrease
            update(lastValues, isFastForward=isFastForward, isRedo=true)
        }

        fun redo(threshIncrease: Int, isFastForward: Boolean=true, checkIters: Boolean=true,
                 isLocalRedo: Boolean=false): Boolean {
//            logd("REDO CSF")
            if (checkIters) {
                if (remainingIters <= 0) return true
                --remainingIters
                logd("iter $remainingIters")
            }
            if (inViewModel.redo(threshIncrease, isFastForward=true))
                return true
            update(lastValues, isFastForward, isRedo=isLocalRedo)
            return false
        }

        data class SegmentPointIndex(val segIdx: Int, val pointIdx: Int)
        data class Connection(val a: SegmentPointIndex,
                              val b: SegmentPointIndex,
                              val intersection: Point) {
            val spis = arrayOf(a, b)
        }

        val connections = arrayListOf<Connection>()
        //val segments = arrayListOf<Segment>()

        private fun drawToPreview(radius: Int) {
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            for (c in connections) {
                circle(previewMat, c.intersection, radius, Colors.blue, 5)
            }

            for ((i, s) in inSegments.withIndex()) {
                val isUsed = connections.any { it.a.segIdx == i || it.b.segIdx == i }
                //logd("isUsed($i): $isUsed")
                val col = if (isUsed) Colors.green else Colors.red
                s.drawTo(previewMat, col, 3)
            }

            dirty = false
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            update(args, isFastForward, isRedo=false)
        }

        private var dirty = true
        fun update(args: IntArray, isFastForward: Boolean, isRedo: Boolean) {
            dirty = true
            val radius = args[0].toDouble()
            val angleDelta = args[1].toRad()
            //see https://www.desmos.com/calculator/ndlnb2quy1
            val cosDelta = cos(PI/2 - angleDelta)
            val radius2 = radius*radius

            fun checkSegments(si1: Int, si2: Int): Boolean {
                val s1 = inSegments[si1]
                val s2 = inSegments[si2]

                val intersection = s1.lineIntersect(s2) ?: return false

                //doesn't handle collinearity but we don't care
                fun doIntersect(): Boolean {
                    val a = s1.p1
                    val b = s1.p2
                    val c = s2.p1
                    val d = s2.p2


                    fun ccw(a: Point, b : Point, c: Point): Boolean {
                        return (c.y-a.y) * (b.x-a.x) > (b.y-a.y) * (c.x-a.x)
                    }
                    return ccw(a,c,d) != ccw(b,c,d) && ccw(a,b,c) != ccw(a,b,d)
                }
                //if (doIntersect()) return true

                //we choose the segment ends that are closest to the intersection
                val (pi1, d1) = (0..1).argminValPair { dist2(s1.points[it], intersection) }
                val (pi2, d2) = (0..1).argminValPair { dist2(s2.points[it], intersection) }

                //if (d1+ d2 < radius2*2) {
                if (d1 < radius2 || d2 < radius2|| doIntersect()) {
                    connections.add(Connection(
                        SegmentPointIndex(si1, pi1),
                        SegmentPointIndex(si2, pi2),
                        intersection))
                    return true
                }
                return false
            }
            connections.clear()
            for ((i, s1) in inSegments.withIndex()) {
                val dir1 = s1.normalizedDir()
                for (j in (i+1) until inSegments.size) {
                    val s2 = inSegments[j]
                    val dir2 = s2.normalizedDir()
                    val dot = dir1.dot(dir2)
                    if (abs(dot) < cosDelta) {
                        checkSegments(i, j)

                        /*if (checkSegments(i, j))
                            logi("dotto: $dot")*/
                    }
                    //val dist =
                    //if ()
                }
            }
            //if we call redo from another fragment it means we know what we're doing and we shouldn't check
            if (!isRedo) {
                when {
                    connections.size < 4 -> redo(-5, isFastForward = isFastForward, isLocalRedo=true)
                    connections.size < 2 -> redo(-10, isFastForward = isFastForward, isLocalRedo=true)

                    connections.size > 20 -> redo(5, isFastForward = isFastForward, isLocalRedo=true)
                    connections.size > 25 -> redo(20, isFastForward = isFastForward, isLocalRedo=true)
                    connections.size > 35 -> redo(30, isFastForward = isFastForward, isLocalRedo=true)

                    else -> {
//                        if (!isFastForward)
//                            drawToPreview(args[2], radius.toInt())
                    }
                }
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                if (dirty || previewMat.width() == 0) {
                    drawToPreview(lastValues[0])
                }

                frag.setImagePreview(previewMat)
            }
        }
    }
}
