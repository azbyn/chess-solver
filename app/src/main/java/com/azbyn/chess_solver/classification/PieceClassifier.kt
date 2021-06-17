package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.*
import kotlinx.serialization.Serializable
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc.*
import org.opencv.ml.SVM


//TODO
//TRY deskew https://docs.opencv.org/master/dd/d3b/tutorial_py_svm_opencv.html

data class BoundsD(val x0: Double, val y0: Double, val x1: Double, val y1: Double)

fun wrapSquareNoMargins(fullImg: Mat, result: Mat, bounds: BoundsD, squareSize: Int) {
    val squareSizeD = squareSize.toDouble()
    val dst = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(squareSizeD, 0.0),
        Point(squareSizeD, squareSizeD),
        Point(0.0, squareSizeD)
    )
    val src = MatOfPoint2f(
        Point(bounds.x0, bounds.y0),
        Point(bounds.x1, bounds.y0),
        Point(bounds.x1, bounds.y1),
        Point(bounds.x0, bounds.y1)
    )
    val m = getPerspectiveTransform(src, dst) //perspective(src, dst)


    //warpAffine(baseMat, previewMat, m, Size(squareSize, squareSize))
    warpPerspective(fullImg, result, m, Size(squareSizeD, squareSizeD))
}
internal fun wrapSquareWithMargins(fullImg: Mat, result: Mat, bounds: BoundsD, squareSize: Int) {
    val dx = (bounds.x1 - bounds.x0)/2
    val dy = (bounds.y1 - bounds.y0)/2
    wrapSquareNoMargins(fullImg, result, BoundsD(
        x0 = bounds.x0 - dx,
        x1 = bounds.x1 + dx,
        y0 = bounds.y0 - dy,
        y1 = bounds.y1 + dy
    ), squareSize)
}

fun pieceImageToVector(fullImg: Mat, bounds: BoundsD, imageType: ImageType): Vector {
    val res = Mat()
    val wrapSquare = when (imageType.useMargins) {
        MarginType.NoMargin -> ::wrapSquareNoMargins
        MarginType.UseMargin -> ::wrapSquareNoMargins
    }
    wrapSquare(fullImg, res, bounds, imageType.squareSize)

    res.convertTo(res, CV_32F)

    return Vector(res.reshape(1, 1))
}

