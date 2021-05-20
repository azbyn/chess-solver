package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.json.JSONObject
import org.opencv.core.Core.*
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import kotlin.math.abs

class OrientationFragment : BaseSlidersFragment(
    SliderData("Rotate", default=0, min=0, max=1, stepSize=1)
//    , SliderData("Reverse lines", default=0, min=0, max=1, stepSize=1)
    ) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Orientation"

    class VM : SlidersViewModel() {
//        private val inViewModel: SmallSquaresFragment.VM by viewModelDelegate()
        private val inViewModel: LineMerge2Fragment.VM by viewModelDelegate()

        //        private val inViewModel: PerspectiveFragment.VM by viewModelDelegate()
        private val baseMat get() = getViewModel<PerspectiveFragment.VM>().resultMat
        val resultMat = Mat()
        private var didRotate = false

        private var previewMat = Mat()

        var xCoords: DoubleArray = doubleArrayOf()
        var yCoords: DoubleArray = doubleArrayOf()

//        private val rect get() = inViewModel.rect

        override fun saveData(sliderDatas: Array<SliderData>) = /*super.saveData(sliderDatas)*/JSONObject().apply {
            put("didRotate", didRotate)
            put("xCoords", xCoords.toStr())
            put("yCoords", yCoords.toStr())
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val extraRotate = args[0] != 0
//            val reverseLines = args[1] != 0

//            val numSquares = 8
//            val xIncrement = rect.width / numSquares
//            val yIncrement = rect.height / numSquares

//            logd("rect: $rect")

            //top left is always white
            //and all edit_square with (x+y) = 1 (mod 2) are also white

            fun getRect(i: Int, j: Int, hori: DoubleArray, vert: DoubleArray): CvRect {
//                val x0 = inViewModel.outHori[i] //rect.x + i * xIncrement
//                val y0 = inViewModel.outVert[j] //rect.y + j * yIncrement
//
//                val w = inViewModel.outHori[i+1] - x0
//                val h = inViewModel.outVert[j+1] - y0
                val x0 = hori[i] //rect.x + i * xIncrement
                val y0 = vert[j] //rect.y + j * yIncrement

                val w = hori[i+1] - x0
                val h = vert[j+1] - y0

//                    val rect = CvRect(x0, y0, xIncrement, yIncrement)
                return CvRect(x0.toInt(), y0.toInt(), w.toInt(), h.toInt())
            }

            val averages = arrayOf(0.0, 0.0)
            for (i in 0 until 8) {
                for (j in 0 until 8) {
                    val rect = getRect(i, j, inViewModel.outHori, inViewModel.outVert)

//                    logd("Überreichteck: $rect")

                    val average = mean(baseMat.submat(rect))[0]
                    //rectangle(previewMat, rect, Colors.red, 5, FILLED)
                    averages[(i+j)%2] += average
                }
            }
            logd("og: v:${inViewModel.outVert[0]} h:${inViewModel.outHori[0]}")
            val shouldRotate = (averages[0] < averages[1]) xor extraRotate
            if (shouldRotate) {
                //logd("ROT8, m8")
                //test _that_ one
//                transpose(baseMat, resultMat)
                rotate(baseMat, resultMat, ROTATE_90_COUNTERCLOCKWISE)

                xCoords = inViewModel.outHori
                yCoords = inViewModel.outVert.reversed().map { resultMat.height() - it }.toDoubleArray()
            } else {
                baseMat.copyTo(resultMat)

                xCoords = inViewModel.outVert
                yCoords = inViewModel.outHori
                //copyTo(baseMat, resultMat,)
            }

            logd("og: x:${xCoords[0]} y:${yCoords[0]}")

            this.didRotate = shouldRotate
            logi("rotate?: $didRotate")

            if (!isFastForward) {
                cvtColor(/*base*/resultMat, previewMat, COLOR_GRAY2RGB)
                val cutoff = abs(averages[0] - averages[1]) / (8 * 8 / 2)
                logi("co: $cutoff")
                /*var startX = rect.x
                var startY = rect.y
                var xInc = xIncrement
                var yInc = yIncrement
                if (shouldRotate) {
                    startX = rect.y
                    startY = rect.x
                    xInc = yIncrement
                    yInc = xIncrement
                }*/
                for (i in 0 until 8) {
                    for (j in 0 until 8) {
                        val rect = getRect(i, j, xCoords, yCoords)

                        val average = mean(/*baseMat*/resultMat.submat(rect))[0]
                        //logd("mean[$i, $j]: $average")
                        if (average < cutoff) {
                            val col = if ((i+j)%2==0) Colors.red else Colors.green
                            rectangle(previewMat, rect, col, -1)
                            averages[(i + j) % 2]
                        }
                    }
                }
                //
                val ogMat = previewMat
                for (c in yCoords) {
                    line(previewMat, Point(0.0, c), Point(ogMat.width().toDouble(), c),
                        Colors.darkMagenta, 3)
                }
                for (c in xCoords) {
                    line(previewMat, Point(c, 0.0), Point(c, ogMat.height().toDouble()),
                        Colors.darkMagenta, 3)
                }
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(previewMat)
            }
        }
    }
}
