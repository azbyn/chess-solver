package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.Misc.logi
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

//ax+by+c=0
data class ABCLine(val a: Double, val b: Double, val c: Double) {
    private constructor(a: Double, b: Double, p0: Point)
            : this(a, b, c=-a*p0.x-b*p0.y) {
        //logi("now a= $a, b=$b, c = $c")
    }
    constructor(p1: Point, p2: Point) : this(
        a = p1.y - p2.y,
        b = p2.x - p1.x,
        p0=p1)
    private val sqrtThing = sqrt(a*a+b*b)

    companion object {
        fun fromDirPoint(dir: Point, p: Point)
                = ABCLine(a=dir.y, b=-dir.x, p0=p)
    }

    fun dist(p: Point): Double {
        return abs(a *p.x + b*p.y+c)/sqrtThing
    }
    fun intersect(other: ABCLine): Point? {
        // https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
        // (the Using homogeneous coordinates bit)

        val a2 = other.a
        val b2 = other.b
        val c2 = other.c

        val ap = (b*c2 - b2*c)
        val bp = (a2*c - a*c2)
        val cp = (a*b2 - a2*b)

//        logi("intersect: ($a, $b, $c), ($a2, $b2, $c2)")
//
//        logi("thing: ($ap, $bp, $cp)")
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

        //logd("DRAW a=$a, b=$b, c = $c : $p1 -> $p2")
        line(mat, p1, p2, col, thickness)
    }
}

/*data */class Segment(/*val */p1: Point, /*val*/ p2: Point,
                       val len: Double = dist(p1, p2)) {
    val points = arrayOf(p1, p2)
    val p1 get() = points[0]
    val p2 get() = points[1]

    val line = ABCLine(p1, p2)

    private fun dist(p: Point) = line.dist(p)
    fun normalizedDir() = (p1-p2).normalized()
    fun lineIntersect(s: Segment) = line.intersect(s.line)
    /*init {
        logd("L: $line, (${p1.x}, ${p1.y}) - (${p2.x}, ${p2.y})")
    }*/
    fun shouldMerge(s: Segment, tDelta: Double): Boolean {
        val delta = tDelta * (len+s.len)
        val x1 = dist(s.p1)
        val x2 = dist(s.p2)

        val y1 = s.dist(p1)
        val y2 = s.dist(p2)
        val gamma = (x1+x2+y1+y2)/4 //(dist(s.p1)+dist(s.p2)+s.dist(p1) + s.dist(p2)) / 4
        val thresh = gamma*delta
        /*val res = */
        return len > thresh && s.len > thresh
        //logd("t = ${thresh.format()}, a = ${len.format()}, b = ${s.len.format()}, res = $res")
        //logd("${x1.format()}, ${x2.format()}, ${y1.format()}, ${y2.format()}")
        //return res
    }
    fun drawTo(mat: Mat, col: Scalar, thickness: Int = 3) {
        line(mat, p1, p2, col, thickness)
    }
}

class SuperSegment(s: Segment) {
    private var segments = arrayListOf(s)
    private val biggest get() = segments[0]
    //returns shouldMerge
    fun checkAndAdd(s: Segment, tDelta: Double): Boolean {
        if (!biggest.shouldMerge(s, tDelta)) return false
        //keep the biggest at index 0
        if (s.len > biggest.len) {
            segments.add(0, s)
        } else {
            segments.add(s)
        }
        return true
    }
    fun getPoints(spacing: Double = 10.0): ArrayList<Point> {
        val res = arrayListOf<Point>()
        for (s in segments) {
            val numPoints = (s.len / spacing).toInt()
            val stepX = (s.p2.x - s.p1.x) / numPoints
            val stepY = (s.p2.y - s.p1.y) / numPoints
            for (i in 0 until numPoints) {
                res.add(Point(s.p1.x + stepX * i, s.p1.y+stepY * i))
            }
        }
        return res
    }
    fun mergedSegments(): Segment {
        var direction = Point(0.0,0.0)
        var averageP = Point(0.0,0.0)
        val threshold = 50
        for (s in segments) {
            var dir = s.p2 - s.p1
            // we want the directions to cancel each other
            //if ()
            if (dir.x < -threshold) dir *= -1
            else if (dir.y < -threshold) dir *= -1
            direction += dir
            averageP +=s.p1+s.p2
        }
        //val minMaxIdx =
        averageP /= segments.size * 2.0
        direction /= direction.norm()
        //we have ||direction|| = 1

        //see segmentMerge.pdf (in romanian)
        val minMax = MinMax()
        //val minMax = MinMaxT(Point())


        for (s in segments) {
            minMax.check(/*s.p1, */(s.p1-averageP).dot(direction))
            minMax.check(/*s.p2, */(s.p2-averageP).dot(direction))
        }
//        val p1 = minMax.minIdx //minMax.min * direction + averageP
//        val p2 = minMax.maxIdx//minMax.max * direction + averageP

        val p1 = minMax.min * direction + averageP
        val p2 = minMax.max * direction + averageP

//        logi("points: $p1, $p2")


        // tl;dr we look for the furthest points from the center
        // with the projection on the line
        //return ABCLine.fromDirPoint(direction, averageP)
        return Segment(p1, p2)
    }
}

class ProbabilisticLineMergeFragment : BaseSlidersFragment(
    SliderData("p", default=50, min = 5, max = 150, stepSize =5),
    SliderData("spacing", default=10, min = 5, max = 100, stepSize =5)
    //SliderData("i", default=0, min = 0, max = 500, stepSize =1)
    //SliderData("close", default=2, min=0, max=5, stepSize=1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Probabilistic Line Merge"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
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
            //val prevTh = lastValues[0]
            super.update(args, isFastForward)
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)

            val proc = args[0] / 100.0
            val spacing = args[1].toDouble()
            //val idx = args[2]
            //logd("lines: ${lines.size()}")
            val area = ogMat.size().area()
            val omega = PI / (2* area.pow(1 / 4.0))
            val tDelta = proc * omega
            val superSegments = ArrayList<SuperSegment>()
            fun addToSuperSegments(s: Segment, tDelta: Double) {
                for (ss in superSegments) {
                    if (ss.checkAndAdd(s, tDelta)) return
                }
                superSegments.add(SuperSegment(s))
            }
            val buf = IntArray(4)
            for (i in 0 until inLines.rows()) {
                inLines.get(i, 0, buf)
                val p1 = Point(buf[0].toDouble(), buf[1].toDouble())
                val p2 = Point(buf[2].toDouble(), buf[3].toDouble())
                addToSuperSegments(Segment(p1, p2), tDelta)
            }
            //logd("size1 = ${inLines.rows()}")
            //logd("size = ${superSegments.size}")
            segments.clear()
            for ((i, ss) in superSegments.withIndex()) {
                val col = Colors.getNiceColor(i)
                val col2 = Colors.getNiceColor(i+1)
                val col3 = Colors.getNiceColor(i+2)
                /*for (s in ss.segments) {
                    line(previewMat, s.p1, s.p2, col, 3)
                }*/
                val points = ss.getPoints(spacing)
                if (!isFastForward) {
                    for (p in points) {
                        circle(previewMat, p, 1, col2, 5)
                    }
                }
                val s = ss.mergedSegments() //fitLine2d(points)
                segments.add(s)
                //lines.add(line)
                if (!isFastForward) {
                    s.drawTo(previewMat, col3, 3)
                }
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

