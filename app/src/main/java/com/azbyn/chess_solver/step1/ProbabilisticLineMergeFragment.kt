package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

//ax+by+c=0
data class Line(val a: Double, val b: Double, val c: Double) {
    private constructor(a: Double, b: Double, p0: Point)
            : this(a, b, c=-a*p0.x-b*p0.y)
    constructor(p1: Point, p2: Point) : this(
        a = p1.y - p2.y,
        b = p2.x - p1.x,
        p0=p1)
    private val sqrt_aabb = sqrt(a*a+b*b)

    companion object {
        fun fromDirPoint(dir: Point, p: Point)
                = Line(a=dir.y, b=-dir.x, p0=p)
    }

    fun dist(p: Point): Double {
        return abs(a *p.x + b*p.y+c)/sqrt_aabb
    }
    fun intersect(other: Line): Point? {
        // https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Using_homogeneous_coordinates

        val a2 = other.a
        val b2 = other.b
        val c2 = other.c

        val ap = (b*c2 - b2*c)
        val bp = (a2*c - a*c2)
        val cp = (a*b2 - a2*b)

        if (abs(cp) < 1e-5) return null
        return Point(ap/cp, bp/cp)
    }

    fun drawTo(mat: Mat, col: Scalar, thickness: Int = 3) {
        val p1 = Point()
        val p2 = Point()

        if (abs(b) >= 1e-5) {
            p1.x = 0.0
            p1.y = -c / b

            p2.x = mat.width().toDouble()
            p2.y = -(a*p2.x+c)/b
        } else {
            p1.y = 0.0
            p1.x = -c / a

            p2.y = mat.height().toDouble()
            p2.x = -(b*p2.y+c)/a
        }

        line(mat, p1, p2, col, thickness)
    }
}

data class Segment(val p1: Point, val p2: Point,
                   val len: Double = dist(p1, p2)) {
    val line = Line(p1, p2)
    val points = arrayOf(p1, p2)

    private fun dist(p: Point) = line.dist(p)
    fun normalizedDir() = (p1-p2).normalized()
    fun lineIntersect(s: Segment) = line.intersect(s.line)

    fun shouldMerge(s: Segment, thresh: Double): Boolean {

        val x1 = dist(s.p1)
        val x2 = dist(s.p2)

        val y1 = s.dist(p1)
        val y2 = s.dist(p2)
        val avrg = (x1 + x2 + y1 + y2) / 4

        return avrg < thresh
    }
    fun drawTo(mat: Mat, col: Scalar, thickness: Int = 3) {
        line(mat, p1, p2, col, thickness)
    }
}

class SegmentGroup(s: Segment) {
    private var segments = arrayListOf(s)
    private val biggest get() = segments[0]

    //returns true if shouldMerge
    fun checkAndAdd(s: Segment, thresh: Double): Boolean {
        if (!biggest.shouldMerge(s, thresh)) return false
        //keep the biggest at index 0
        if (s.len > biggest.len) {
            segments.add(0, s)
        } else {
            segments.add(s)
        }
        return true
    }

    fun toSingleSegment(): Segment {
        var direction = Point(0.0, 0.0)
        var averageP = Point(0.0, 0.0)

        for (s in segments) {
            var dir = s.p2 - s.p1

            val angle = atan2(dir.y, dir.x) // in (-pi, pi]
            if (angle < 0)
                dir *= -1

            direction += dir
            averageP +=s.p1+s.p2
        }

        averageP /= segments.size * 2.0

        direction /= direction.norm()
        //now we have ||direction|| = 1

        //see segmentMerge.pdf (in romanian)
        val minMax = MinMax()

        for (s in segments) {
            minMax.check((s.p1-averageP).dot(direction))
            minMax.check((s.p2-averageP).dot(direction))
        }
        val p1 = minMax.min * direction + averageP
        val p2 = minMax.max * direction + averageP

        return Segment(p1, p2)
    }
}

class ProbabilisticLineMergeFragment : BaseSlidersFragment(
    SliderData("thresh", default=15, min = 2, max = 60, stepSize =2),
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Line Merge"

    class VM : SlidersViewModel() {
        private val inViewModel: ProbabilisticLineFragment.VM by viewModelDelegate()
        private val inLines get() = inViewModel.lines
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()

        val segments = arrayListOf<Segment>()

        fun redo(threshIncrease: Int, isFastForward: Boolean=true): Boolean {
            if (inViewModel.redo(threshIncrease, isFastForward=true))
                return true
            update(lastValues, isFastForward)
            return false
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            val thresh = args[0].toDouble()// / 100.0
            val groups = ArrayList<SegmentGroup>()

            fun addToSegmentGroups(s: Segment, thresh: Double) {
                for (g in groups) {
                    if (g.checkAndAdd(s, thresh)) return
                }
                groups.add(SegmentGroup(s))
            }
            val buf = IntArray(4)
            for (i in 0 until inLines.rows()) {
                inLines.get(i, 0, buf)
                val p1 = Point(buf[0].toDouble(), buf[1].toDouble())
                val p2 = Point(buf[2].toDouble(), buf[3].toDouble())
                addToSegmentGroups(Segment(p1, p2), thresh)
            }

            segments.clear()
            for ((i, group) in groups.withIndex()) {
                val col = Colors.getNiceColor(i)
                val s = group.toSingleSegment()
                segments.add(s)
                if (!isFastForward) {
                    s.drawTo(previewMat, col, 3)
                }
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

