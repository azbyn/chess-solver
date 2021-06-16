package com.azbyn.chess_solver.svm

import org.opencv.core.Core
import org.opencv.core.CvType.CV_32S
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.ml.Ml
import org.opencv.ml.SVM
import kotlin.math.roundToInt

//fun Mat.toDoubleArray() =  DoubleArray(width()* height()).apply {
//    this@toDoubleArray.get(0, 0, this)
//}
//fun Mat.toFloatArray() = FloatArray(width()* height()).apply {
//    this@toFloatArray.get(0, 0, this)
//}

fun trainSingleOpenCv(settings: SvmSettings, X: List<Vector>, y: List<Double>): Svm {
    val svm = SVM.create()
    svm.type = SVM.C_SVC
//    svm.setType(cv2.ml.SVM_NU_SVC)
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
            svm.degree = settings.K.d.toDouble()
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

//    svm.setKernel(SVM.POLY)
//    svm.degree = 1.0
//    svm.coef0 = 0.0
//    svm.gamma = 1.0

    svm.c = settings.C
    svm.termCriteria = TermCriteria(TermCriteria.MAX_ITER /*and TermCriteria.EPS*/, settings.maxiter, settings.tol)

    val n  = X.first().size

    // Set up training data
//    val labels = intArrayOf(1, -1, -1, -1)
    val trainingPoints = Mat()
    Core.vconcat(X.map { it.toMat() }, trainingPoints)

    val trainingCats = Mat(y.size, 1, CV_32S)
    trainingCats.put(0, 0, y.map{ it.roundToInt() }.toIntArray())

//    val trainingCats = Mat(y.size, 1, CvType.CV_32SC1)
//    trainingCats.put(0, 0, y.toIntArray())
//
//    val yy = y.map { it.toFloat() }

//    val trainingCats = MatOfInt(*y.toIntArray())

//    println("${trainingPoints.type()} vs $CV_32F vs $CV_32S")
//    println("${trainingCats.type()} vs $CV_32F vs $CV_32S")

//    svm.trainAuto(trainingPoints, Ml.ROW_SAMPLE, trainingCats)
    svm.train(trainingPoints, Ml.ROW_SAMPLE, trainingCats)
    println("svm c: ${svm.c}")
    println("svm γ: ${svm.gamma}")

//    svm.train(trainingPoints, ROW_SAMPLE, trainingCats)

    val supportVectorMat: Mat = svm.supportVectors
//    println("sv: ${supportVectorMat.size()}")
    val sv = (0 until supportVectorMat.rows()).map { i ->
//        println("SVI ${supportVectorMat.row(i).toDoubleArray().toList()}")
        Vector(supportVectorMat.row(i)) }


    val alpha = Mat()
    val svidx = Mat()
    val rho = svm.getDecisionFunction(0, alpha, svidx)// Mat())
//    val weigths = svm.alpha
//    println("weights: ${alpha.size()}, ${alpha.toDoubleArray().toList()}")
//    println("svidx: ${svidx.size()} ${svidx[0,0].toList()}")
//    println("rho: $rho")


    return Svm(
        K = Kernel.Linear,
        supportVectors = sv,
//        bias = -rho,
//        weights = alpha.toDoubleArray().toList()

        bias = rho,
        weights = alpha.toDoubleArray().map { -it }
    )

//     supportVectors.row()
//    println("support vectors: ${supportVectorMat.rows()}")
//

//    exitProcess(43)
//    throw Exception("WIP")
//    return OpenCvSvm(svm)
}