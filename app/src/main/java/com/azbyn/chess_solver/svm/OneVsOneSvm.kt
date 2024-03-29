package com.azbyn.chess_solver.svm

import kotlinx.serialization.Serializable

@Serializable
data class OneVsOneSvm(val subSvms: List<SubSvm>, val classes: List<Int>): MultiSvm() {
    @Serializable
    data class SubSvm(val class0: Int, val class1: Int, val svm: Svm)

    override fun classifyChoices(x: Vector): List<ClassificationResult<Int>> {
        val classesTally = HashMap<Int, Int>()
        for (c in this.classes)
            classesTally[c] = 0

        for ((c0, c1, svm) in subSvms) {
            val (isC0, _) = svm.classify(x)
            val cl = if (isC0) c0 else c1
            classesTally[cl] = classesTally[cl]!! + 1
        }

        return classesTally.toList().sortedByDescending { (_, v) -> v  }
            .map { (c, count) -> ClassificationResult(c, certainty =count.toDouble()) }
    }
}

fun trainOVO(settings: SvmSettings, X: List<Vector>, y: List<Int>, algorithm: SvmAlgorithm): OneVsOneSvm {
    val classes = y.distinct()

    val subSvms = mutableListOf<OneVsOneSvm.SubSvm>()

    for ((i, c0) in classes.withIndex()) {
        for (c1 in classes.subList(i+1, classes.size)) {
            val newX = mutableListOf<Vector>()
            val newY = mutableListOf<Double>()

            loop@ for ((xi, yi) in (X zip y)) {
                val newYi = when (yi) {
                    c0 -> 1.0
                    c1 -> -1.0
                    else -> continue@loop
                }
                newX.add(xi)
                newY.add(newYi)
            }

            subSvms.add(OneVsOneSvm.SubSvm(c0, c1, algorithm(settings, newX, newY)))
        }
    }
    return OneVsOneSvm(subSvms, classes)
}