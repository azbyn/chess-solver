package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable
import org.opencv.ml.SVM
import java.io.File

/*sealed*/ abstract class MultiSvm {
    abstract fun classifyChoices(x: Vector): List<ClassificationResult<Int>>
    fun classify(x: Vector) = classifyChoices(x).first()

}