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
    data class Gaussian(val sigma: Double): Kernel() {
        override fun invoke(x: Vector, y: Vector) = exp(-dist2(x, y) / (2 * sigma * sigma))
    }

    @Serializable
    data class Polynomial(val a: Double, val c: Double, val d: Int): Kernel() {
        override fun invoke(x: Vector, y: Vector) = (a * dot(x, y) + c).pow(d)
    }
}
