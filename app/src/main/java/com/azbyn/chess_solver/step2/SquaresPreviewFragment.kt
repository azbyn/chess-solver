package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.CvType.CV_8U
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.*

class SquaresPreviewFragment : BaseSlidersFragment(
//    SliderData("i", default=13/*9*/, min=1, max=91, stepSize=2),
    SliderData("i", default=0, min=0, max=63, stepSize=1)//,
//    SliderData("Blur", default=5, min=1, max=31, stepSize=2)
) {
    //override val prevFragment = FragmentIndex.PERSPECTIVE
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Squares Preview - debug"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
//        private val inViewModel: SmallSquaresFragment.VM by viewModelDelegate()
//        private val inViewModel: SmallSquaresFragment.VM by viewModelDelegate()
        private val inViewModel: OrientationFragment.VM by viewModelDelegate()


        private val baseMat get() = inViewModel.resultMat
        private val previewMat = Mat()

        //        private val didRotate get() = getViewModel<OrientationFragment.VM>().didRotate
        private val xCoords get() = inViewModel.xCoords
        private val yCoords get() = inViewModel.yCoords


        companion object {
            const val squareSize = 16.0
        }
//        private val rect get() = inViewModel.rect


        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val i = args[0]

            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(squareSize, 0.0),
                Point(squareSize, squareSize),
                Point(0.0, squareSize)
            )
            val x = i % 8
            val y = i / 8

//            var startX = rect.x
//            var startY = rect.y
//            var xInc = rect.width / 8
//            var yInc = rect.height / 8
//
//            if (didRotate) {
//                startX = rect.y
//                startY = rect.x
//                xInc = rect.height / 8
//                yInc = rect.width / 8
//            }
            //
            val x0 = xCoords[x]
            val y0 = yCoords[y]

            val x1 = xCoords[x+1]
            val y1 = yCoords[y+1]

//                    val rect = CvRect(x0, y0, xIncrement, yIncrement)
//            val x0 = (startX + xInc*x).toDouble()
//            val y0 = (startY + yInc*y).toDouble()

            val src = MatOfPoint2f(
                Point(x0, y0),
                Point(x1, y0),
                Point(x1, y1),
                Point(x0, y1)
//                Point(x0+xInc, y0),
//                Point(x0+xInc, y0+yInc),
//                Point(x0,      y0+yInc)
            )
            //val points2
            val m = getPerspectiveTransform(src, dst) //getAffineTransform(src, dst)


            //warpAffine(baseMat, previewMat, m, Size(squareSize, squareSize))
            warpPerspective(baseMat, previewMat, m, Size(squareSize, squareSize))
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(previewMat)
            }
        }
    }
}
