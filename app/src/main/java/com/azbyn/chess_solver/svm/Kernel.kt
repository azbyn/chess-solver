package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.pow

@Serializable
sealed class Kernel {
    abstract operator fun invoke(x: Vector, y: Vector): Double

    @Serializable
    object Linear: Kernel() {
        override fun invoke(x: Vector, y: Vector) = dot(x, y)
    }

    @Serializable
    data class Gaussian(val gamma: Double): Kernel() {
        override fun invoke(x: Vector, y: Vector) = exp(-dist2(x, y) * gamma)
    }

    @Serializable
    data class Polynomial(val a: Double, val c: Double, val d: Double): Kernel() {
        override fun invoke(x: Vector, y: Vector) = (a * dot(x, y) + c).pow(d)
    }
    @Serializable
    data class Sigmoid(val gamma: Double, val r: Double): Kernel() {
        //alpha = 1/dim
        override fun invoke(x: Vector, y: Vector) = (gamma * dot(x, y) + r)
    }
    @Serializable
    data class Chi2(val gamma: Double): Kernel() {
        override fun invoke(x: Vector, y: Vector): Double {
            var sum = 0.0
            for (i in 0 until x.size) {
                sum += (x[i] - y[i]).pow(2) / (x[i] + y[i])
            }
            return 1 - 2 * sum
        }
    }
}