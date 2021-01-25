package com.azbyn.chess_solver

import org.opencv.core.CvType.CV_8U
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*

class MorphologicalFragment : BaseSlidersFragment(
    SliderData("ker", default = 9, min = 1, max = 31, stepSize = 2),
    SliderData("iter",  default = 2, min = 1, max = 10, stepSize = 1),
    SliderData("isOpen", default = 0, min = 0, max = 1)
) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Morphological"

    class VM : SlidersViewModel() {
        private val baseMat get() = getViewModel<ThresholdFragment.VM>().resultMat

        var resultMat = Mat()
            private set

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val k = p[0]
            val iter = p[1]
            val op = if (p[2] == 1) MORPH_OPEN else MORPH_CLOSE
            val ker = Mat.ones(k, k, CV_8U)
            morphologyEx(baseMat, resultMat, op, ker, Point(-1.0, -1.0), iter)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
