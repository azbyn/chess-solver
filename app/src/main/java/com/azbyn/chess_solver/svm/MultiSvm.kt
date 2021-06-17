package com.azbyn.chess_solver.svm

/*sealed*/ abstract class MultiSvm {
    abstract fun classifyChoices(x: Vector): List<ClassificationResult<Int>>
    fun classify(x: Vector) = classifyChoices(x).first()
}