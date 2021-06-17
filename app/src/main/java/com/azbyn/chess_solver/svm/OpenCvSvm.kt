package com.azbyn.chess_solver.svm

import org.opencv.core.Core.vconcat
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.ml.Ml.ROW_SAMPLE
import org.opencv.ml.SVM
import org.opencv.ml.SVM.C_SVC
import kotlin.math.roundToInt

data class OpenCvSvm(val svm: SVM): MultiSvm() {
    override fun classifyChoices(x: Vector): List<ClassificationResult<Int>> {
        val res = svm.predict(x.toMat()).roundToInt()
        return listOf(ClassificationResult(res, certainty = 1.0))
    }
}

fun trainOpenCv(settings: SvmSettings, X: List<Vector>, y: List<Int>, algorithm: SvmAlgorithm = ::trainSmo): OpenCvSvm {
    val svm = SVM.create()
    svm.type = C_SVC

    when (settings.K) {
        is Kernel.Gaussian -> {
            svm.setKernel(SVM.RBF)
            svm.gamma = settings.K.gamma
        }
        Kernel.Linear ->
            svm.setKernel(SVM.LINEAR)
        is Kernel.Polynomial -> {
            svm.setKernel(SVM.RBF)
            svm.gamma = settings.K.a
            svm.coef0 = settings.K.c
            svm.degree = settings.K.d
        }
        is Kernel.Sigmoid -> {
            svm.setKernel(SVM.SIGMOID)
            svm.gamma = settings.K.gamma
            svm.coef0 = settings.K.r
        }
        is Kernel.Chi2 -> {
            svm.setKernel(SVM.SIGMOID)
            svm.gamma = settings.K.gamma
        }
    }

    svm.c = settings.C
    svm.termCriteria = TermCriteria(TermCriteria.MAX_ITER /*and TermCriteria.EPS*/, settings.maxiter, settings.tol)

    val trainingPoints = Mat()
    vconcat(X.map { it.toMat() }, trainingPoints)


    val trainingCats = Mat(y.size, 1, CvType.CV_32SC1)
    trainingCats.put(0, 0, y.toIntArray())

    svm.train(trainingPoints, ROW_SAMPLE, trainingCats)
    return OpenCvSvm(svm)
}
