package com.azbyn.chess_solver

import org.opencv.core.MatOfPoint
import org.opencv.core.Point


class MinMaxT<T>(defaultIdx: T, bigVal: Double = 1e10) {
    var minIdx: T = defaultIdx
        private set
    var maxIdx: T = defaultIdx
        private set

    private var minVal = bigVal
    private var maxVal = -bigVal

    fun check(index: T, value: Double) {
        if (value < minVal) {
            minVal = value
            minIdx = index
        } /*else *///we don't use else if as it the first one might be the biggest
        if (value > maxVal) {
            maxVal = value
            maxIdx = index
        }
    }
}

typealias MinMaxIndex = MinMaxT<Int>

class MinMax(bigVal: Double = 1e10) {
    var min = bigVal
    var max = -bigVal

    val delta get() = max - min
    fun check(value: Double) {
        if (value < min) {
            min = value
        }
        //we don't use `else if` as it the first one might be the biggest
        if (value > max) {
            max = value
        }
    }
}

class MinMaxRect(bigVal: Double = 1e10) {
//
    val x = MinMax(bigVal)
    val y = MinMax(bigVal)

    fun makeRect() = CvRect(x.min.toInt(), y.min.toInt(), x.delta.toInt(), y.delta.toInt())
    fun setRect(r: CvRect) {
        r.x = x.min.toInt()
        r.y = y.min.toInt()
        r.width = x.delta.toInt()
        r.height = y.delta.toInt()
    }

    fun check(value: Point) = check(value.x, value.y)

    fun check(x: Double, y: Double ) {
        this.x.check(x)
        this.y.check(y)
    }
    fun checkContour(c: MatOfPoint) {
        // expand the rect to include all points in the MatOfPoint
        val size = c.height()
        for (i in 0 until size) {
            val p = c[i, 0]
            check(p[0], p[1])
        }
    }
}
