package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.opencv.core.Core.mean
import org.opencv.core.Mat

class EdgeFragment : BaseSlidersFragment(
    //SliderData("thrsh1", default = 50/*75*/, min = 0, max = 255*2, stepSize = 5),
    //SliderData("thrsh2", default = 100/*150*/, min = 0, max = 255*2, stepSize = 5)
//    SliderData("σ%", default = 33/*150*/, min = 1, max = 75, stepSize = 2)
    SliderData("offset", default = 0/*150*/, min = -100, max = 100, stepSize = 5)

) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Edge"

    class VM : SlidersViewModel() {
        private val inViewModel: BlurFragment.VM by viewModelDelegate()
//        private val inViewModel: MorphologicalFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        var resultMat = Mat()
            private set

        private var remainingIters = 0

        override fun init(frag: BaseFragment) {
            super.init(frag)
            remainingIters = 10
        }

        fun redo(blurIncrease: Int, isFastForward: Boolean=true, checkIters: Boolean=true) {
            if (checkIters) {
                if (remainingIters <= 0) return
                --remainingIters
                logd("iter $remainingIters")
            }

//        fun redo(blurIncrease: Int, isFastForward: Boolean=true) {
            inViewModel.redo(blurIncrease, isFastForward=true)
            update(lastValues, isFastForward)
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            /*val t1 = p[0].toDouble()
            val t2 = p[1].toDouble()

            Canny(baseMat, resultMat, t1, t2, 3)/
             */
            val offset = args[0].toDouble()

            autoCanny(baseMat, resultMat, offset)
            val average = AcceptFragment.VM.convertArea(mean(resultMat)[0])
//            logi("авереджь: $average")
            when {
                average > 10 -> redo(8, isFastForward)
                average > 8 -> redo(4, isFastForward)
                average > 6 -> redo(2, isFastForward)
                average < 2 -> redo(-2, isFastForward)
//                average < 1 -> redo(-4, isFastForward)

            }

//            val sigma = args[0] * .01
//            autoCanny(baseMat, resultMat, sigma)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
