package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.MultiSvm
import com.azbyn.chess_solver.svm.Vector

interface DoubleClassifier {
    fun classify(emptyV: Vector, multiV: Vector): Int
}

data class ClassifierWithIsEmpty(val eSvm: IsEmptyClassifier, val mSvm: MultiSvm): DoubleClassifier {
    override fun classify(emptyV: Vector, multiV: Vector): Int {
        if (eSvm.isEmptySquare(emptyV))
            return Piece.Nothing.toClass()
        return mSvm.classify(multiV).result
    }
}