package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable

@Serializable
data class OneVsAllSvm(val svms: List<Pair<Int, Svm>>): MultiSvm() {
    override fun classifyChoices(x: Vector): List<ClassificationResult<Int>> {
        return svms.map { (cl, svm) ->
            ClassificationResult(cl, svm.classify(x).certainty)
        }.sortedByDescending { it.certainty }
    }
}

fun trainOVA(settings: SvmSettings, X: List<Vector>, y: List<Int>, algorithm: SvmAlgorithm = ::trainSmo): OneVsAllSvm {
    val classes = y.distinct()
    fun train1(cl: Int): Svm {
        val newY = List(y.size) { i -> if (y[i]==cl) 1.0 else -1.0 }
        return algorithm(settings, X, newY)
    }
    if (classes.size == 2) {
        val (c1, c2) = classes
        val svm1 = train1(c1)
        val svm2 = Svm(
            weights = svm1.weights.map { w -> -w },
            bias = -svm1.bias,
            supportVectors =  svm1.supportVectors,
            K = svm1.K)
        return OneVsAllSvm(listOf(
            Pair(c1, svm1),
            Pair(c2, svm2)
        ))
    }

    return OneVsAllSvm(List(classes.size) { i -> Pair(classes[i], train1(classes[i])) })
}