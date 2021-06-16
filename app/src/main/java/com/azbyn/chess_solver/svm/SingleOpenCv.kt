package com.azbyn.chess_solver.svm

import org.opencv.core.Core
import org.opencv.core.CvType.CV_32S
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.ml.Ml
import org.opencv.ml.SVM
import kotlin.math.roundToInt


fun trainSingleOpenCv(settings: SvmSettings, X: List<Vector>, y: List<Double>): Svm {
    val svm = SVM.create()
    svm.type = SVM.C_SVC
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
    Core.vconcat(X.map { it.toMat() }, trainingPoints)

    val trainingCats = Mat(y.size, 1, CV_32S)
    trainingCats.put(0, 0, y.map{ it.roundToInt() }.toIntArray())

    svm.train(trainingPoints, Ml.ROW_SAMPLE, trainingCats)

    val supportVectorMat: Mat = svm.supportVectors
    val sv = (0 until supportVectorMat.rows()).map { i ->
        Vector(supportVectorMat.row(i))
    }


    val alpha = Mat()
    val svidx = Mat()
    val rho = svm.getDecisionFunction(0, alpha, svidx)


    return Svm(
        K = Kernel.Linear,
        supportVectors = sv,

        bias = rho,
        weights = alpha.toDoubleArray().map { -it }
    )
}