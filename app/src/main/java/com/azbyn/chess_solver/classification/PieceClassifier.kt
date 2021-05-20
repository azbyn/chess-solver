package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.*
import kotlinx.serialization.Serializable
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc.*
import org.opencv.ml.SVM

const val squareSize = 32.0
const val useMargins = false
//const val useMargins = true

//TODO
//TRY deskew https://docs.opencv.org/master/dd/d3b/tutorial_py_svm_opencv.html

@Serializable
data class PieceClassifier<Impl: MultiSvm>(val multiSvm: Impl)

//    fun toPieceResult(res: ClassificationResult<Int>): ClassificationResult<Piece> {
//        val (cl, value) = res
//        return ClassificationResult(Piece.fromClass(cl), value)
//    }
fun PieceClassifier<OpenCvSvm>.writeTo(path: String) {
    multiSvm.svm.save(path)
}



fun pieceClassifierFromFile(path: String): PieceClassifier<OpenCvSvm> {
    return PieceClassifier(OpenCvSvm(SVM.load(path)))
}

//TODO y: list piece?
//fun trainPieceClassifier(settings: SvmSettings, X: List<Vector>, y: List<Int>, algorithm: SvmAlgorithm = ::trainSmo2):
//        PieceClassifier {
//    return PieceClassifier(trainOpenCv(settings, X, y))
//}
//

fun <T: MultiSvm> PieceClassifier<T>.classifyChoices(x: Vector): List<Piece> =
    multiSvm.classifyChoices(x).map(Piece.Companion::fromClass)

fun <T: MultiSvm> PieceClassifier<T>.classify(x: Vector): Piece = Piece.fromClass(multiSvm.classify(x))

private val dst = MatOfPoint2f(
    Point(0.0, 0.0),
    Point(squareSize, 0.0),
    Point(squareSize, squareSize),
    Point(0.0, squareSize)
)

data class BoundsD(val x0: Double, val y0: Double, val x1: Double, val y1: Double)

fun wrapSquareNoMargins(fullImg: Mat, result: Mat, bounds: BoundsD) {
    val src = MatOfPoint2f(
        Point(bounds.x0, bounds.y0),
        Point(bounds.x1, bounds.y0),
        Point(bounds.x1, bounds.y1),
        Point(bounds.x0, bounds.y1)
    )
    val m = getPerspectiveTransform(src, dst) //perspective(src, dst)


    //warpAffine(baseMat, previewMat, m, Size(squareSize, squareSize))
    warpPerspective(fullImg, result, m, Size(squareSize, squareSize))
}
private fun wrapSquareWithMargins(fullImg: Mat, result: Mat, bounds: BoundsD) {
    val dx = (bounds.x1 - bounds.x0)/2
    val dy = (bounds.y1 - bounds.y0)/2
    wrapSquareNoMargins(fullImg, result, BoundsD(
        x0 = bounds.x0 - dx,
        x1 = bounds.x1 + dx,
        y0 = bounds.y0 - dy,
        y1 = bounds.y1 + dy
    ))
}


val wrapSquare = if (useMargins) ::wrapSquareWithMargins else ::wrapSquareNoMargins


fun pieceImageToVector(fullImg: Mat, bounds: BoundsD): Vector {
    val res = Mat()
    wrapSquare(fullImg, res, bounds)
//    res.convertTo(res, CV_32F, 1/255.0)
    res.convertTo(res, CV_32F)
    return Vector(res.reshape(1, 1))// res.rows() * res.cols()))
}

