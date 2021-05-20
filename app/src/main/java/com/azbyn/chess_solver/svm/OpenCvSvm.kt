package com.azbyn.chess_solver.svm

import org.opencv.core.Core.vconcat
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.ml.Ml.ROW_SAMPLE
import org.opencv.ml.SVM
import org.opencv.ml.SVM.C_SVC
import org.opencv.ml.SVM.LINEAR
import kotlin.math.roundToInt


//@Serializable
data class OpenCvSvm(val svm: SVM): MultiSvm() {
    override fun classifyChoices(x: Vector): List<Int> {
        val res = svm.predict(x.toMat()).roundToInt()
        return listOf(res)
//        return svms.map { (cl, svm) ->
//            ClassificationResult(cl, svm.classify(x).marginDistance)
//        }.sortedBy { it.marginDistance }.map { it.result }
    }
}


fun trainOpenCv(settings: SvmSettings, X: List<Vector>, y: List<Int>, algorithm: SvmAlgorithm = ::trainSmo): OpenCvSvm {
    val svm = SVM.create()
    svm.type = C_SVC
//    svm.setType(cv2.ml.SVM_NU_SVC)
    svm.setKernel(LINEAR)
    svm.c = settings.C
    svm.termCriteria = TermCriteria(TermCriteria.MAX_ITER /*and TermCriteria.EPS*/, settings.maxiter, settings.tol)

//    val n  =  X.first().size

    // Set up training data
//    val labels = intArrayOf(1, -1, -1, -1)
    val trainingPoints = Mat()
    vconcat(X.map { it.toMat() }, trainingPoints)


    val trainingCats = Mat(y.size, 1, CvType.CV_32SC1)
    trainingCats.put(0, 0, y.toIntArray())

//    val yy = y.map { it.toFloat() }

//    val trainingCats = MatOfInt(*y.toIntArray())

//    println("${trainingCats.type()} vs $CV_32F vs $CV_32S")

    svm.trainAuto(trainingPoints, ROW_SAMPLE, trainingCats)
    println("svm c: ${svm.c}")
//    svm.train(trainingPoints, ROW_SAMPLE, trainingCats)
//
    return OpenCvSvm(svm)
}

// returns a list of results
// the list is sorted by confidence
// so the first result is the one we're most certain of
//fun OneVsAllSvm.classifyChoices(x: Vector): List<Int> {
//    return svms.map { (cl, svm) ->
//        ClassificationResult(cl, svm.classify(x).marginDistance)
//    }.sortedBy { it.marginDistance }.map { it.result }
//}
//fun OneVsAllSvm.classify(x: Vector) = classifyChoices(x).first()
//fun OneVsAllSvm.classify(x: Vector): ClassificationResult<Int> {
//    return svms.map { (cl, svm) ->
//        ClassificationResult(cl, svm.classify(x).marginDistance)
//    }.maxWithOrNull(compareBy { it.marginDistance })!!
//}