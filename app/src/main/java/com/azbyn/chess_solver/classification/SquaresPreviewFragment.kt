package com.azbyn.chess_solver.classification

import android.graphics.Rect
import android.graphics.RectF
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step2.OrientationFragment
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.roundToInt

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


        private val fullMat get() = inViewModel.resultMat
        lateinit var boardImage: BoardImage

        private var previewMat = Mat()


        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val i = args[0]
            boardImage = BoardImage(fullMat, inViewModel.xCoords, inViewModel.yCoords)

            val x = i % 8
            val y = i / 8
            val bounds = boardImage.getPieceBounds(x, y)
            wrapSquare(fullMat, previewMat, bounds)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(previewMat)
            }
        }
    }
}
