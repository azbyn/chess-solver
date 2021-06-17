package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*

class PerspectiveFragment : BaseSlidersFragment(
    SliderData("size", default = 1024, min = 256, max = 4096, stepSize = 256)//,
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Perspective"

    class VM : SlidersViewModel() {
        private val inViewModel: EditSquareFragment.VM by viewModelDelegate()

        private val ogMat get() = getViewModel<AcceptFragment.VM>().bigMat
        private val points get() = inViewModel.points
        var resultMat = Mat()
            private set


        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val size = args[0].toDouble()

            val ratio = AcceptFragment.VM.ratio
            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(size, 0.0),
                Point(size, size),
                Point(0.0, size)
            )

            val src = MatOfPoint2f(*(points.map { it / ratio }).toTypedArray())// *x.toTypedArray()) //( p1, p2, p3, p4)
            val m = getPerspectiveTransform(src, dst)

            warpPerspective(ogMat, resultMat, m, Size(size, size))
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(resultMat)
            }
        }
    }
}
