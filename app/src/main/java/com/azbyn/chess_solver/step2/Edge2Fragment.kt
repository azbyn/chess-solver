package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.Core.mean
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class Edge2Fragment : BaseSlidersFragment(
    SliderData("Blur", default=7, min=1, max=91, stepSize=2),
    SliderData("offset", default = 0/*150*/, min = -100, max = 100, stepSize = 5)
) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Edge 2"

    class VM : SlidersViewModel() {
        private val inViewModel: PerspectiveFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        var resultMat = Mat()
            private set

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val blurVal = args[0]
            val offset = args[1].toDouble()
            Imgproc.medianBlur(baseMat, resultMat, blurVal)

            autoCanny(resultMat, resultMat, offset)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
