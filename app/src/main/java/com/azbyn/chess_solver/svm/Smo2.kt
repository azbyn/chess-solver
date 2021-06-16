package com.azbyn.chess_solver.svm

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.system.exitProcess

fun clamp(v: Double, min: Double, max: Double) =
    min(max, max(v, min))

@Suppress("LocalVariableName")
fun trainSmo2(settings: SvmSettings, X_: List<Vector>, y_: List<Double>): Svm {
    var it = 0
    var y = y_
    var X = X_
    var alpha = MutableList(y.size) { 0.0 }
    var bias = 0.0

    val K = settings.K
    val tol = settings.tol
    val C = settings.C
    while (it++ < settings.maxiter) {
        var changedAlphas = 0
        for (i in X.indices) {
            val Ei = kernelSum(X[i], X, alpha, y, K) -y[i]

            if (!((Ei*y[i] <-tol) && alpha[i]<C)||(Ei*y[i] > tol && (alpha[i] > 0))) {
                continue
            }
            for (j in X.indices) {
                if (i == j) continue

                val Ej = kernelSum(X[j], X, alpha, y, K) -y[j]

                val oldAi=alpha[i]
                val oldAj=alpha[j]

                var L: Double
                var H: Double

                if (y[i] == y[j]) {
//                    #s = y_i y_j =1
                    L = max(0.0, alpha[i] + alpha[j] - C)
                    H = min(C, alpha[i] + alpha[j])
                } else {
//                    # s = y_i y_j = - 1
                    L = max(0.0, alpha[j] - alpha[i])
                    H = min(C, C + alpha[j] - alpha[i])
                }
//                if (y[i]!=y[j]) {
//                    L = max(0.0, alpha[j] - alpha[i])
//                    H = min(C, C + alpha[j] - alpha[i])
//                } else {
//                    L=max(0.0,alpha[i]+alpha[j]-C)
//                    H=min(C,alpha[i]+alpha[j])
//                }
                if (L==H)
                    continue

                // this looks wrong

                val Kii = K(X[i], X[i])
                val Kij = K(X[i], X[j])
                val Kjj = K(X[j], X[j])

                val eta = 2 * Kij - Kii * Kjj

                alpha[j] += (y[j] * (Ei - Ej)) / eta

                alpha[j] = clamp(alpha[j], L, H)

                if (abs(alpha[j]-oldAj) < tol)
                    continue

                alpha[i] -= y[i]*y[j]*(alpha[j]-oldAi)
                val bi = bias - Ei - y[i]*(alpha[i]-oldAi)* Kii - y[j]*(alpha[j]-oldAj)* Kij
                val bj = bias - Ej - y[i]*(alpha[i]-oldAi)* Kij - y[j]*(alpha[j]-oldAj)* Kjj
                bias = if (0.0< alpha[i] && alpha[i] < C) {
                    bi
                } else if (0.0 <alpha[j] && alpha[j]<C) {
                    bj
                } else {
                    (bi + bj) / 2
                }
                changedAlphas += 1
            }
        }
        if (changedAlphas == 0) break

        val newX = ArrayList<Vector>()
        val newY = ArrayList<Double>()
        val newAlpha = ArrayList<Double>()

        for ((i, ai) in alpha.withIndex()) {
            if (ai == 0.0) continue
            if (ai.isNaN()) {

                println("NaN $it")
                exitProcess(0)
            }
            newX.add(X[i])
            newAlpha.add(ai)
            newY.add(y[i])
        }
        X = newX.toList()
        y = newY.toList()
        alpha = newAlpha.toMutableList()
    }

    //pre-multiplied yi*alpha_i
    val weights = List(y.size) { i -> y[i] * alpha[i] }

    fun calculateBias(): Double {
        var res = 0.0
        for ((i, xi) in X.withIndex()) {
            res += y[i] - kernelSum(xi, X, weights, K)
        }
        return res / X.size
    }

    return Svm(weights = weights, supportVectors = X, bias = calculateBias(), K=K)
}
