package com.azbyn.chess_solver

import com.azbyn.chess_solver.step1.BlurFragment
import org.opencv.core.CvType.CV_8U
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.*

class ThresholdFragment : BaseSlidersFragment(
    SliderData("thrsh", default = 128/*100*/, min = 0, max = 255, stepSize = 1),
    SliderData("type",  default = 1, min = 0, max = 4, stepSize = 1),
    SliderData("invert", default=0, min=0, max=1)
) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Threshold"

    override fun initImpl() {
        super.initImpl()
        viewModel.lastValues[0] = -1
    }

    class VM : SlidersViewModel() {
        private val baseMat get() = getViewModel<BlurFragment.VM>().resultMat
        //private val baseMat get() = getViewModel<AcceptFragment.VM>().resultMat

        var resultMat = Mat()
            private set

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val th = p[0].toDouble()
            val type = p[1]
            val inv = p[2]//TODO - POINTLESS
            fun also(i : Int) {
                val k = Mat.ones(i, i, CV_8U)
                erode(resultMat, resultMat, k)
                dilate(resultMat, resultMat, k)
            }
            val tt = if (inv==1) THRESH_BINARY_INV else THRESH_BINARY
            when (type) {
                0 -> threshold(baseMat, resultMat, th, 255.0, tt)
                1 -> threshold(baseMat, resultMat, th, 255.0, THRESH_OTSU or tt)
                2 -> threshold(baseMat, resultMat, th, 255.0, THRESH_TRIANGLE or tt)
                3 -> {adaptiveThreshold(baseMat, resultMat, 255.0, ADAPTIVE_THRESH_MEAN_C,
                        tt, 3, 0.0); also(p[0]) }
                4 -> {adaptiveThreshold(baseMat, resultMat, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C,
                    tt, 3, 0.0); also(p[0]) }
            }
            //val blur = p[2].toDouble()

            //Canny(baseMat, resultMat, t1, t2, 3)
            //blur(resultMat, resultMat, Size(blur, blur))
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
