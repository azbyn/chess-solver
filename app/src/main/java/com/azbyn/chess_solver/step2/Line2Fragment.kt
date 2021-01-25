package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.Line
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.*
import kotlin.math.*

class Line2Fragment : BaseSlidersFragment(
    SliderData("thrsh", default = 100/*100*//*75*/, min = 0, max = 255*2, stepSize = 5),
    SliderData("angle", default=5, min = 1, max = 30, stepSize =1)
//    SliderData("close", default=1, min=0, max=5, stepSize=1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Line 2"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
        private val inViewModel: Edge2Fragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        private val ogMat get() = getViewModel<PerspectiveFragment.VM>().resultMat
        private var previewMat = Mat()
        val horiLines = arrayListOf<Line>()
        val vertLines = arrayListOf<Line>()

        //var resultMat = Mat()
        //    private set
//        var angleDeg = 0.0
//            private set
//        private var firstUpdate = true

//        override fun init(frag: BaseFragment) {
//            super.init(frag)
//            firstUpdate = true
//        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            //val prevTh = lastValues[0]
            super.update(args, isFastForward)
            val lines = Mat()

            val th = args[0]
            val angle = args[1].toRad()
//            val bucketCount = p[1]
//            val closeNuff = p[2]
            //if (firstUpdate || th != prevTh) {
            //    firstUpdate = false
                //logd("OIDA")
            HoughLines(baseMat, lines, 1.0, PI / 180.0, th)
            //}

            //ogMat.copyTo(resultMat)
            cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
            horiLines.clear()
            vertLines.clear()

            for (i in 0 until lines.rows() step 2) {
                val buf = lines.get(i, 0)
                val line = Line(rho=buf[0], theta=buf[1])
//                logd("angle ${line.theta} - ${line.theta.toDeg()} - ${line.theta.toRad()}")
                val col = when {
                    line.theta < angle || line.theta > PI - angle -> {
                        vertLines.add(line)
                        Colors.green
                    }
                    abs(line.theta- PI/2) < angle -> {
                        horiLines.add(line)
                        Colors.blue
                    }
                    else -> {
                        Colors.red
                    }
                }

                line.drawTo(previewMat, col)
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

