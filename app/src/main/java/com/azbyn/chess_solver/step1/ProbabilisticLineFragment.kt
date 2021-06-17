package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logd
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

class ProbabilisticLineFragment : BaseSlidersFragment(
    SliderData("thrsh", default = 125/*100*//*75*/, min = 0, max = 255*2, stepSize = 5),
    SliderData("len", default=70, min = 5, max = 600, stepSize =5),
    SliderData("gap", default=20, min = 1, max = 60, stepSize =2)
    //SliderData("close", default=2, min=0, max=5, stepSize=1)
) {

    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Probabilistic Line"

    override var humanModified: Boolean
        get() = super.humanModified
        set(value) {
            logd("humanModified = $value")

            viewModel.humanModified = value
            super.humanModified = value
        }


    class VM : SlidersViewModel() {
        private val inViewModel: EdgeFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()
        var lines = Mat()
            private set

        var humanModified = false

        fun redo(threshIncrease: Int, isFastForward: Boolean): Boolean {
            logd("redo.humanModified = $humanModified")
            if (humanModified) {
                logd("human modified")
                return true
            }
            lastValues[0] += threshIncrease
            logd("redo line: ${lastValues[0]}")
            update(lastValues, isFastForward)
            return false
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)

            val thresh = args[0]
            val length = args[1]
            val gap = args[2]
            HoughLinesP(
                baseMat, lines, 1.0, PI / 180, thresh,
                length.toDouble(), gap.toDouble()//20.0
            )

            if (!isFastForward) {
                cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
                logd("len = ${lines.rows()}")
                val buf = IntArray(4)
                val col = Colors.green
                for (i in 0 until lines.rows()) {
                    lines.get(i, 0, buf)
                    val p1 = Point(buf[0].toDouble(), buf[1].toDouble())
                    val p2 = Point(buf[2].toDouble(), buf[3].toDouble())
                    line(previewMat, p1, p2, col, 3)
                }
            }
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(previewMat)// setImageGrayscalePreview(resultMat)
            }
        }
    }
}
