package com.azbyn.chess_solver

import android.graphics.PointF
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.lang.Exception
import kotlin.math.*

operator fun Point.plus(p: Point) = Point(x+p.x, y+p.y)
operator fun Point.minus(p: Point) = Point(x-p.x, y-p.y)
operator fun Point.div(a: Double) = Point(x/a, y/a)
operator fun Point.div(a: Int) = Point(x/a, y/a)

operator fun Point.times(a: Double) = Point(x*a, y*a)
operator fun Point.times(a: Int) = Point(x*a, y*a)
operator fun Scalar.get(i: Int) = this.`val`[i]

operator fun Double.times(p: Point) = Point(p.x*this, p.y*this)
operator fun Int.times(p: Point) = Point(p.x*this, p.y*this)

fun Point.normalized() = this/this.norm()


fun Point.norm2() = x*x+y*y
fun Point.norm() = sqrt(norm2())

fun Point.copy() = Point(x,y)

//distance squared
fun dist2(a: Point, b: Point): Double {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx*dx + dy * dy
}
fun dist2(a: PointF, b: Point): Double {
    val x = a.x - b.x
    val y = a.y-b.y
    return x*x+y*y
}


fun dist(a: Point, b: Point): Double {
    return sqrt(dist2(a, b))
}
fun area(points: Array<Point>): Double {
    //calculated with Gauß' area formula:
    //A = 1/2 * abs(sum_{i=1}^{n-1} (x_i * y_{i+1} - x_{i+1} * y_i) +x_n * y_1- x_1*y_n)
    val p0 = points[0]
    val pn = points[points.size-1]
    var res = pn.x * p0.y - p0.x * pn.y
    for (i in 0 until (points.size-1)) {
        res += (points[i].x * points[i+1].y) -(points[i+1].x * points[i].y)
    }
    res *= .5
    return abs(res)
}
fun Iterable<Int>.argmin(f: (x: Int) -> Double): Int {
    var minI = -1
    var minVal = Double.MAX_VALUE

    for (i in this) {
        val newVal = f(i)
        if (newVal < minVal) {
            minI = i
            minVal = newVal
        }
    }

    return minI
}
fun Iterable<Int>.argminValPair(f: (x: Int) -> Double): Pair<Int, Double> {
    var minI = -1
    var minVal = Double.MAX_VALUE

    for (i in this) {
        val newVal = f(i)
        if (newVal < minVal) {
            minI = i
            minVal = newVal
        }
    }

    return Pair(minI, minVal)
}

fun colorMapAndNormalize(src: Mat, dst: Mat) {
    src.copyTo(dst)
    //normalize(src,dst, 0.0, 255.0, NORM_MINMAX)
    //applyColorMap(src, dst, COLORMAP_VIRIDIS)
//    cv::normalize(src, dst, 0.0, 255.0, cv::NORM_MINMAX);
//    cv::applyColorMap(dst, dst, getColorMap());
}


// https://www.pyimagesearch.com/2015/04/06/zero-parameter-automatic-canny-edge-detection-with-python-and-opencv/
//fun autoCanny(src: Mat, dst: Mat, sigma: Double, apertureSize:Int=3) {
//    val median = mean(src)[0]
//    val t2 = threshold(src, dst, 0.0, 255.0, THRESH_BINARY + THRESH_OTSU)
//    val t1 = t2/2 // max(0.0, (1-sigma)*median)
////    val t2 = t2 = min(255.0, (1+sigma)*median)
//    //logi("median: $median => ($t1, $t2)")
//    Canny(src, dst, t1, t2, apertureSize)
//}

fun autoCanny(src: Mat, dst: Mat, offset: Double, apertureSize:Int=3) {
    //val median = mean(src)[0]
    val t2 = Imgproc.threshold(src, dst, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU) + offset
    val t1 = t2/2 // max(0.0, (1-sigma)*median)
//    val t2 = t2 = min(255.0, (1+sigma)*median)
    //logi("median: $median => ($t1, $t2)")
    Imgproc.Canny(src, dst, t1, t2, apertureSize)
}

fun Iterable<Double>.angleAverage(): Double {
    var sinSum = 0.0
    var cosSum = 0.0
    var n = 0
    for (a in this) {
        sinSum += sin(a)
        cosSum += cos(a)
        ++n
    }
    return atan2(sinSum/n, cosSum/n)
}

fun <E> MutableList<E>.removeRange(startIdx: Int, endIdx: Int) {
    try { //logd("oi, ちょっと まって $startIdx: $size - $endIdx")
        for (i in startIdx until min(endIdx, size)) {
            ///logd("remove $startIdx / $size")
            removeAt(startIdx)
        }
    }
    catch (e: Exception) {
        Misc.logw("rr $startIdx->$endIdx", e)
    }
}
fun <E> MutableList<E>.removeCount(startIdx: Int, cnt: Int) {
    try { //logd("oi, ちょっと まって $startIdx: $size - $endIdx")
        for (i in 0 until cnt) {
            removeAt(startIdx)
        }
    }
    catch (e: Exception) {
        Misc.logw("rc $startIdx $cnt ", e)
    }
}




/*
fun morphologicalSkeleton(src: Mat, dst: Mat) =
    JniImpl.morphologicalSkeleton(src.nativeObj, dst.nativeObj)

object JniImpl {
    external fun blobbing(maskAddr: Long, result: ArrayList<Mat>): IntArray?

    external fun bitwiseAndBlobs(boundsArr: IntArray, blobs: ArrayList<Mat>, imgAddr: Long,
                                 dilateVal: Int, erodeVal: Int)

    external fun bitwiseAndSingleBlob(x: Int, y: Int, w: Int, h: Int,
                                      blobAddr: Long, imgAddr: Long,
                                      dilateVal: Int, erodeVal: Int)

    external fun linesExtract(matAddr: Long, linesAddr: Long, outputAddr: Long,
                              thresh: Int, length: Double, rejectAngle: Double)


    external fun colorMapAndNormalize(srcAddr: Long, dstAddr: Long)
    external fun morphologicalSkeleton(srcAddr: Long, dstAddr: Long)


    // superLinesGetDensity(lines.nativeObj, colored.nativeObj, minLength=p[0],
// slineSize=p[1], rejectAngle=rejectAngle, desiredDensity=DESIRED_DENSITY)
    external fun superLinesGetDensity(linesAddr: Long, outputAddr: Long,
                                      minLength: Int, slineSize: Int, rejectAngle: Double): Int


    external fun superLinesRemoval(linesAddr: Long, outputAddr: Long,
                                   mids: Long,
                                   width: Int, height: Int,
                                   minLength: Int, slineSize: Int,
                                   rejectAngle: Double, scale: Double)

    external fun newSlineMids(): Long
    external fun delSlineMids(addr: Long)


    external fun getLinesMask(resultAddr: Long, thickness: Int,
                              midsAddr: Long, width: Int, height: Int)


    external fun getDesiredDensity(): Int

    //external fun matrixScreenToDrawable(mat: FloatArray, vec: FloatArray)
    //external fun matrixDrawableToScreen(mat: FloatArray, vec: PointF)
}
*/