package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable


data class SvmSettings(val K: Kernel, val C: Double = 10.0, val tol: Double = 1e-3, val maxiter: Int = 1000) {
    fun withChangedC(newC: Double) = SvmSettings(K=K, C=newC, tol=tol, maxiter=maxiter)
    fun withChangedKernel(newK: Kernel) = SvmSettings(K=newK, C=C, tol=tol, maxiter=maxiter)

}

@Serializable
data class Svm(val weights: List<Double>,
               val bias: Double,
               val supportVectors: List<Vector>,
               val K: Kernel)

typealias SvmAlgorithm = (settings: SvmSettings, X_: List<Vector>, y_: List<Double>) -> Svm

fun kernelSum(x: Vector, supportVectors: List<Vector>, weights: List<Double>, K: Kernel): Double {
    var res = 0.0
    for ((i, xi) in supportVectors.withIndex()) {
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

data class ClassificationResult<T>(val result: T, val certainty: Double) {
    override fun toString() = "$result -> c:$certainty"
}

fun Svm.classify(x: Vector): ClassificationResult<Boolean> {
    val res = kernelSum(x, this.supportVectors, this.weights, this.K) + bias

    return ClassificationResult(res >= 0.0, res)
}
