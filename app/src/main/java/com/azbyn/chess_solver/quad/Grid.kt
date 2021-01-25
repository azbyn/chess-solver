package com.azbyn.chess_solver.quad

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logeSimple
import com.azbyn.chess_solver.Misc.logi
import com.azbyn.chess_solver.Misc.logw
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.line
import kotlin.math.sqrt

class Grid(quad1: PointQuad) {
    class VisitIndexManager {
        private var visitIndex = 0
        fun get() = visitIndex
        fun getAndIncrement() = visitIndex++
    }
    private val vim = VisitIndexManager()
    private val quads = arrayListOf(GridQuad(quad1))
    private val quad get() = quads[0]
    /*
    private val quad = GridQuad(quad1) //quads[0]
    private var empty = false
    fun isEmpty() = empty
    fun clear() {empty= true}
     */

    fun isEmpty() = quads.isEmpty()
    fun clear() = quads.clear()
    fun getCount() = quad.getCount(vim.getAndIncrement())

    fun getAverageLineSize() = quad.getAverageLineSize(vim.getAndIncrement())

    fun tryMerge(g: Grid, centerEps: Double, eps: Double): Boolean {
        //for (q in g.quads) {
        val q = g.quad
        // NOT COMPLETED, TODO tryMerge for neighbours
            if (tryAdd(q, centerEps, eps)) {
                q.tryAddNeighbours(this, centerEps, eps, vim.getAndIncrement())
                return true
            }
        //}
        return false
    }
    fun tryAdd(newQ: Quad, centerEps: Double, eps: Double): Boolean {
        val newCenter = newQ.center
        for (q in quads) {
            val d = dist2(newCenter, q.center)
            if (d < centerEps) {
                q.addQuad(newQ)
                return true
            }
        }
        return quad.tryAddQuad(newQ, eps, vim.getAndIncrement(), vim)
    }
    fun drawTo(img: Mat, color: Scalar, thickness: Int) =
        quad.drawTo(img, color, thickness, vim.getAndIncrement())

    class GridPoint(x: Double, y: Double, weight: Int = 1) : Point(x, y) {
        var addedPoints: Int = weight
            private set
        constructor(p: Point, weight: Int = 1): this(p.x, p.y, weight)

        fun addPoint(p: Point, weight: Int=1) {
            this.x = this.x * addedPoints + p.x * weight
            this.y = this.y * addedPoints + p.y * weight
            addedPoints += weight
            this.x /= addedPoints
            this.y /= addedPoints
        }

        fun addPoint(p: GridPoint) {
            addPoint(p, weight=p.addedPoints)
        }
        fun getPointClone() = Point(x, y)
    }
    class GridQuad(private val points: Array<GridPoint>,
                   private var visitIndex: Int = -1,
                   private var visitIndex2: Int = -1,
                   private val neighbours: Array<GridQuad?> = arrayOfNulls(4)): Quad {

        constructor(q: Quad) : this(Array(4) { GridPoint(q[it]) })
        //constructor(q: Quad, i: Int, p: GridPoint) :
        //        this(Array(4) { if (it==i) p else GridPoint(q[it]) })
        fun getAverageLineSize(visitIndex: Int): Double {
            val res = AverageCounter()
            getAverageLineSizeImpl(visitIndex, res)
            return res.sum / res.count
        }

        fun getCount(visitIndex: Int): Int {
            if (this.visitIndex == visitIndex) return 0
            this.visitIndex = visitIndex
            var res = 1
            for (n in neighbours) {
                if (n != null) res += n.getCount(visitIndex)
            }
            return res
        }
        private data class AverageCounter(var sum: Double=0.0, var count: Int=0)
        private fun getAverageLineSizeImpl(visitIndex: Int, res: AverageCounter) {
            if (this.visitIndex == visitIndex) return
            this.visitIndex = visitIndex

            //some lines are counted multiple times but that's ok
            for (i in 0 until 4) {
                val p1 = points[i]
                val p2 = points[(i+1) %4]
                val len = sqrt(dist2(p1, p2))
                res.sum += len
                neighbours[i]?.getAverageLineSizeImpl(visitIndex, res)
            }
            res.count += 4
        }
        fun tryAddNeighbours(g: Grid, centerEps: Double, eps: Double, visitIndex: Int) {
            //if (this.visitIndex == visitIndex) return
            //this.visitIndex = visitIndex
            for (n in neighbours) {
                n?.tryAddNeighboursImpl(g, centerEps, eps, visitIndex)
            }
        }
        private fun tryAddNeighboursImpl(g: Grid, centerEps: Double, eps: Double, visitIndex: Int) {
            if (this.visitIndex == visitIndex) return
            this.visitIndex = visitIndex
            val res = g.tryAdd(this, centerEps, eps)
            if (!res) logw("Merge Failed (tryAdd failed)")
            for (n in neighbours) {
                n?.tryAddNeighboursImpl(g, centerEps, eps, visitIndex)
            }
        }
        override fun getWeight(i: Int) = points[i].addedPoints
        override val center: Point
            get() {
                val res = points[0].getPointClone()
                for (i in 1 until 4) {
                    val p = points[i]
                    res.x += p.x
                    res.y += p.y
                }
                res.x /= 4
                res.y /= 4
                return res
            }
        override operator fun get(i: Int) = points[i]

        private fun setBothWays(i: Int, v: GridQuad?) {
            neighbours[i] = v
            // 0 <-> 2, 1 <-> 3
            if (v != null) v.neighbours[i xor 2] = this
        }
        fun addQuad(q: Quad) {
            for (i in 0 until 4) {
                points[i].addPoint(q[i], q.getWeight(i))
            }
        }
        fun drawTo(img: Mat, color: Scalar, thickness: Int, visitIndex: Int) {
            if (this.visitIndex == visitIndex) return
            this.visitIndex = visitIndex
            val colors = arrayOf(Colors.blue, Colors.green, Colors.red, Colors.cyan)
            for (i in 0..3) {
                //line(img, points[i], points[(i+1) % 4], color, thickness)
                line(img, points[i], points[(i+1) % 4], colors[i], thickness)
                neighbours[i]?.drawTo(img, color, thickness, visitIndex)
            }
        }

        fun tryAddQuad(q: Quad, eps: Double, visitIndex: Int, vim: VisitIndexManager): Boolean {
            if (this.visitIndex == visitIndex) return false
            this.visitIndex = visitIndex
            if (tryAddQuadImpl(q, eps, vim.getAndIncrement())) return true
            for (n in neighbours) {
                if (n?.tryAddQuad(q, eps, visitIndex, vim) == true) return true
            }
            return false
        }

        private fun tryAddQuadImpl(q: Quad, eps: Double, visitIndex2: Int): Boolean {
            for ((i, p) in points.withIndex()) {
                val otherIndex = i xor 2 //or equivalently (quadIndex + 2) % 2
                if (dist2(q[otherIndex], p) < eps) {
                    val array = Array(4) {
                        if (it==i) p else pOrClose(q, it, eps, visitIndex2)
                    }
                    setBothWays(i, GridQuad(array))
                    return true
                }
            }
            return false
        }
        private fun pOrClose(q: Quad, i: Int, eps: Double, visitIndex2: Int): GridPoint {
            val res = pOrCloseImpl(q, i, eps, visitIndex2)
            return res ?: GridPoint(q[i], q.getWeight(i))
        }

        private fun pOrCloseImpl(q: Quad, i: Int, eps: Double, visitIndex2: Int): GridPoint? {
            if (this.visitIndex2 == visitIndex2) return null
            this.visitIndex2 = visitIndex2
            val p = points[i xor 2]
            if (dist2(p, q[i]) < eps) {
                p.addPoint(q[i], q.getWeight(i))
                return p
            }
            for (n in neighbours) {
                val res = n?.pOrCloseImpl(q, i, eps, visitIndex2)
                if (res != null) return res
            }
            return null
        }
    }
}
