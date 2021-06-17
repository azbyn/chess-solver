package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.dist
import com.azbyn.chess_solver.toRad
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.line
import kotlin.math.PI
import kotlin.math.atan2

interface Quad {
    operator fun get(i: Int): Point
    fun getWeight(i: Int): Int
    val center: Point
}

class PointQuad(val points: Array<Point>): Quad {
    constructor(func: (i: Int)-> Point) : this(
        points = Array(4, func))

    override fun getWeight(i: Int) = 1
    override val center: Point by lazy {
        val res = points[0].clone()
        for (i in 1 until 4) {
            val p = points[i]
            res.x += p.x
            res.y += p.y
        }
        res.x /= 4
        res.y /= 4
        return@lazy res
    }
    fun area(): Double = com.azbyn.chess_solver.area(points)
    // sort by angle : from left-most in cw order
    // 0   1
    // 3   2
    //
    //     1
    //  0     2
    //     3
    // it could also be:
    //   0
    // 3   1
    //   2
    // but we check for the smallest angle to fix this, and replace it with case 2
    fun sortCW(angleCloseEnough: Double = 15.toRad()) {
        //var maxAngle = -100.0
        var minAngle = 100.0
        points.sortWith(compareBy {
            val a = atan2(it.y - center.y, it.x - center.x)
            //if (a > maxAngle) maxAngle = a
            if (a < minAngle) minAngle = a
            return@compareBy a
        })
        if (PI + minAngle < angleCloseEnough) {
            //0 1 2 3 becomes 1 2 3 0
            val temp = points[0]
            for (i in 0 until 3) {
                points[i] = points[i+1]
            }
            points[3] = temp
            //logi("SWAPPED")
        }
        //logi("sort: min: ${minAngle / PI * 180}, max ${maxAngle / PI* 180}")
    }

    fun drawTo(img: Mat, color: Scalar, thickness: Int) {
        for (i in 0 until 4) {
            val p0 = points[i]
            val p1 = points[(i +1) % 4]
            line(img, p0, p1, color, thickness)
        }
    }

    @Suppress("unused")
    fun clone() = PointQuad(points.clone())

    operator fun iterator() = points.iterator()
    override operator fun get(i: Int) = points[i]

    override fun toString() = "(q $center)" //points.toStr()

    //width and height
    fun dimensions(): Size {
        sortCW()
        return Size(
            dist(points[0], points[1]),
            dist(points[1], points[2])
        )
    }
}
