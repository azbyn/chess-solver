package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step2.OrientationFragment
import org.opencv.core.*

class SquaresPreviewFragment : BaseSlidersFragment(
    SliderData("i", default=0, min=0, max=63, stepSize=1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Squares Preview - debug"

    class VM : SlidersViewModel() {
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
            wrapSquareWithMargins(fullMat, previewMat, bounds, 32)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(previewMat)
            }
        }
    }
}
