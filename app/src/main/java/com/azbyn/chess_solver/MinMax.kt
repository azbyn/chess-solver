package com.azbyn.chess_solver

import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point


class MinMaxT<T>(defaultIdx: T, bigVal: Double = 1e10) {
    var minIdx: T = defaultIdx
        private set
    var maxIdx: T = defaultIdx
        private set

    private var minVal = bigVal
    private var maxVal = -bigVal

//    var minVal = bigVal
//         private set
//    var maxVal = -bigVal
//         private set


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
        } /*else *///we don't use else if as it the first one might be the biggest
        if (value > max) {
            max = value
        }
    }
}
class MinMaxRect(bigVal: Double = 1e10) {
    val x = MinMax(bigVal)
    val y = MinMax(bigVal)

    fun makeRect() = CvRect(x.min.toInt(), y.min.toInt(), x.delta.toInt(), y.delta.toInt())
    fun setRect(r: CvRect) {
        r.x = x.min.toInt()
        r.y = y.min.toInt()
        r.width = x.delta.toInt()
        r.height = y.delta.toInt()
    }


    //var min = bigVal
    //var max = -bigVal

    fun check(value: Point) = check(value.x, value.y)

    fun check(x: Double, y: Double ) {
        this.x.check(x)
        this.y.check(y)
    }
    fun checkContour(c: MatOfPoint) {
        val size = c.height()
        //logi("sz: $size")
        /*approx2f.convertTo(approx, CV_32S)
            val area = abs(contourArea(approx))*/
        for (i in 0 until size) {
            val p = c[i, 0]
            //logi("pt: $p")
            check(p[0], p[1])
            //circle(mat, Point(p[0], p[1]), 3,  Scalar(255.0))
        }
    }
}
