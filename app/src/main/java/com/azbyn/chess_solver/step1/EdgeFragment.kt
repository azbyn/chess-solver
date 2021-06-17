package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.opencv.core.Core.mean
import org.opencv.core.Mat

class EdgeFragment : BaseSlidersFragment(
    SliderData("offset", default = 0/*150*/, min = -100, max = 100, stepSize = 5)

) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Edge"

    class VM : SlidersViewModel() {
        private val inViewModel: BlurFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        var resultMat = Mat()
            private set

        private var remainingIters = 0

        override fun init(frag: BaseFragment) {
            super.init(frag)
            remainingIters = 10
        }

        private fun redo(blurIncrease: Int, isFastForward: Boolean=true, checkIters: Boolean=true) {
            if (checkIters) {
                if (remainingIters <= 0) return
                --remainingIters
                logd("iter $remainingIters")
            }

            inViewModel.redo(blurIncrease, isFastForward=true)
            update(lastValues, isFastForward)
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)

            val offset = args[0].toDouble()

            autoCanny(baseMat, resultMat, offset)
            val average = AcceptFragment.VM.convertArea(mean(resultMat)[0])
            when {
                average > 10 -> redo(8, isFastForward)
                average > 8 -> redo(4, isFastForward)
                average > 6 -> redo(2, isFastForward)
                average < 2 -> redo(-2, isFastForward)
//                average < 1 -> redo(-4, isFastForward)
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
