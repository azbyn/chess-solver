package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.json.JSONObject
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs.imwrite
import org.opencv.imgproc.Imgproc.*

class PerspectiveFragment : BaseSlidersFragment(
    SliderData("size", default = 1024, min = 256, max = 4096, stepSize = 256)//,
    //SliderData("flip", default=0, min=0, max=1, stepSize=1)
) {
    //TODO FIND BIGGEST SQUARE or partial square
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Perspective"

    override fun saveData(path: String): JSONObject {
        imwrite("$path/table.png", viewModel.resultMat)
        return super.saveData(path)
    }

    class VM : SlidersViewModel() {
        private val inViewModel: EditSquareFragment.VM by viewModelDelegate()
        //private val baseMat get() = inViewModel.resultMat
        private val ogMat get() = getViewModel<AcceptFragment.VM>().bigMat
        private val points get() = inViewModel.points
        var resultMat = Mat()
            private set
        //private val angleDeg get() = inViewModel.angleDeg

        override fun update(p: IntArray, isFastForward: Boolean) {
            super.update(p, isFastForward)
            val size = p[0]
            //val flip = p[1] != 0
            val sizef = size.toDouble()
            val ratio = AcceptFragment.VM.ratio
            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(sizef, 0.0),
                Point(sizef, sizef),
                Point(0.0, sizef)//modified from the og (this was [1])
            )
            //val points2
            val src = MatOfPoint2f(*(points.map { it / ratio }).toTypedArray())// *x.toTypedArray()) //( p1, p2, p3, p4)
            val m = getPerspectiveTransform(src, dst)
            //javadoc: warpPerspective(src, dst, M, dsize)
            warpPerspective(ogMat, resultMat, m, Size(sizef, sizef))
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImagePreview(resultMat)
            }
        }
    }
}
