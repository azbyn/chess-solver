package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable

@Serializable
sealed class MultiSvm() {
    abstract fun classifyChoices(x: Vector): List<Int>
    fun classify(x: Vector) = classifyChoices(x).first()
}