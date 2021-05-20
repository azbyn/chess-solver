package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable


data class SvmSettings(val K: Kernel, val C: Double = 10.0, val tol: Double = 1e-3, val maxiter: Int = 1000)

@Serializable
data class Svm(val weights: List<Double>,
               val bias: Double,
               val supportVectors: List<Vector>,
               val K: Kernel)

typealias SvmAlgorithm = (settings: SvmSettings, X_: List<Vector>, y_: List<Double>) -> Svm

fun kernelSum(x: Vector, supportVectors: List<Vector>, weights: List<Double>, K: Kernel): Double {
    var res = 0.0
//    println("<>: ${weights} $K")
//    println("<>: svms $supportVectors")
    for ((i, xi) in supportVectors.withIndex()) {
//        println("K ~= ${K(xi, x)}")
        res += weights[i] * K(xi, x)
    }
    return res
}
fun kernelSum(x: Vector, supportVectors: List<Vector>, alpha: List<Double>, y: List<Double>, K: Kernel): Double {
    var res = 0.0

    for ((i, xi) in supportVectors.withIndex()) {
        res += alpha[i] * y[i] * K(xi, x)
    }
    return res
}

data class ClassificationResult<T>(val result: T, val marginDistance: Double)

fun Svm.classify(x: Vector): ClassificationResult<Boolean> {
//    println("classify: $supportVectors")
    val res = kernelSum(x, this.supportVectors, this.weights, this.K) + bias

    return ClassificationResult(res >= 0.0, res)
}