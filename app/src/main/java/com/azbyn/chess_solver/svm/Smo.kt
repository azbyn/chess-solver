package com.azbyn.chess_solver.svm

import java.lang.Exception
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.system.exitProcess

@Suppress("LocalVariableName")
fun trainSmo(settings: SvmSettings, X_: List<Vector>, y_: List<Double>): Svm {
    val K = settings.K
    val C = settings.C
    val tol = settings.tol
    var bias = 0.0

    var it = 0

    var alpha = MutableList<Double>(X_.size) { 0.0 }
    var X = X_
    var y = y_

    fun sum(k: Int): Double {
        var res = 0.0
        for ((i, ai) in alpha.withIndex()) {
            res += ai * y[i] * K(X[k], X[i])
        }
        return res
    }



    while (it < settings.maxiter) {
        it++
        var changedAlphas = 0
        val n = y.size
        for (i in 0 until n) {
            val Ei = sum(i) + bias - y[i]
            val yE = Ei * y[i]
            if (!((alpha[i] < C && yE < -tol) || (alpha[i] > 0 && yE > tol))) {
                // KKT violation
                continue
            }

            for (j in 0 until n) {
                if (i == j) continue
                val Ej = sum(j) + bias - y[j]
                val ai = alpha[i]
                val aj = alpha[j]
                var L: Double
                var H: Double
                if (y[i] == y[j]) {
                    // s = y_i y_j =1
                    L = max(0.0, alpha[i] + alpha[j] - C)
                    H = min(C, alpha[i] + alpha[j])
                } else {
                    // s = y_i y_j = -1
                    L = max(0.0, alpha[j] - alpha[i])
                    H = min(C, C + alpha[j] - alpha[i])
                }
                if (L == H) continue
                val Kii = K(X[i], X[i])
                val Kij = K(X[i], X[j])
                val Kjj = K(X[j], X[j])

                val eta = 2 * Kij - Kii - Kjj

                if (abs(eta) < tol) {
                    alpha[j] = H
                } else {
                    alpha[j] += y[j] * (Ej - Ei) / eta
                    // update alpha_j
                }
                if (abs(alpha[j] - aj) < tol) {
                    // skip if no change
                    continue
                }
                alpha[i] -= y[i] * y[j] * (alpha[j] - aj) // find alpha_i

                val bi = bias - Ei - y[i] * (alpha[i] - ai) * Kii - y[j] * (alpha[j] - aj) * Kij
                val bj = bias - Ej - y[i] * (alpha[i] - ai) * Kij - y[j] * (alpha[j] - aj) * Kjj

                bias = if (0 < alpha[i] && alpha[i] < C) {
                    bi
                } else if (0 < alpha[j] && alpha[i] < C) {
                    bj
                } else {
                    (bi + bj) / 2
                }
                changedAlphas++
            }

        }
        if (changedAlphas == 0)
            break
        val newX = ArrayList<Vector>()
        val newY = ArrayList<Double>()
        val newAlpha = ArrayList<Double>()

        for ((i, ai) in alpha.withIndex()) {
            if (ai == 0.0) continue
            if (ai.isNaN()) {

                println("NaN $it")
                exitProcess(32)
            }
            newX.add(X[i])
            newAlpha.add(ai)
            newY.add(y[i])
        }
        if (newX.size == 0) {
            throw Exception("No support vectors found. try increasing C")
        }
        X = newX.toList()
        y = newY.toList()
        alpha = newAlpha.toMutableList()
    }
    
    //pre-multiplied yi*alpha_i
    val weights = List(y.size) { i -> y[i] * alpha[i] }

    println("w: $weights")

    fun calculateBias(): Double {
        var res = 0.0
        for ((i, xi) in X.withIndex()) {
            res += y[i] - kernelSum(xi, X, weights, K)
        }
        return res / X.size
    }

    return Svm(weights = weights, supportVectors = X, bias = calculateBias(), K=K)
}
