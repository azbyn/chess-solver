package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.*
import kotlinx.serialization.Serializable


@Serializable
data class IsEmptyClassifier(val svm: Svm) {
    fun isEmptySquare(x: Vector) = svm.classify(x).result
}

fun trainEmptyClassifier(settings: SvmSettings, X_: List<Vector>, y_: List<Piece>,
                         algorithm: SvmAlgorithm = ::trainSmo2): IsEmptyClassifier {
    val (X, y) = (X_ zip y_).map {
            (xi, pi) -> Pair(xi, if (pi.isNothing) -1.0 else 1.0)
//        (xi, pi) -> Pair(xi, if (pi.isNothing) 1 else 0)
    }.unzip()

//    return IsEmptyClassifier(trainOpenCv(settings, X, y, algorithm))

    return IsEmptyClassifier(algorithm(settings, X, y))
}
@JvmName("trainEmptyClassifier1")
fun trainEmptyClassifier(settings: SvmSettings, X_: List<Vector>, y_: List<Boolean>,
                         algorithm: SvmAlgorithm = ::trainSmo2): IsEmptyClassifier {
    val (X, y) = (X_ zip y_).map {
            (xi, pi) -> Pair(xi, if (!pi) -1.0 else 1.0)
    }.unzip()

    return IsEmptyClassifier(algorithm(settings, X, y))
}