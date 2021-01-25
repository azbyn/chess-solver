package com.azbyn.chess_solver.step2

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PerspectiveFragment
import org.opencv.core.CvType.CV_8U
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.*

class DilateFragment : BaseSlidersFragment(
    SliderData("Dilate", default=13/*9*/, min=1, max=91, stepSize=2),
    SliderData("Invert", default=1, min=0, max=1, stepSize=1),
    SliderData("Blur", default=5, min=1, max=31, stepSize=2)
) {
    //override val prevFragment = FragmentIndex.PERSPECTIVE
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Dilate"

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
        private val inViewModel: PerspectiveFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        var resultMat = Mat()
            private set

        val dilateAmount get() = lastValues[0]
        val wasInverted get() = lastValues[1] != 0

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            val invert = args[1] != 0
            val kerSize = args[0]
            val blur = args[2]
            //updateImpl(baseMat, resultMat, args[0], args[1])
            val th = 127.0
            val tt = if (invert) THRESH_BINARY_INV else THRESH_BINARY

            medianBlur(baseMat, resultMat, blur)
            threshold(/*baseMat*/resultMat, resultMat, th, 255.0, THRESH_OTSU or tt)
            val k = Mat.ones(kerSize, kerSize, CV_8U)
            //dilate(resultMat, resultMat, k)
            erode(resultMat, resultMat, k)

            //dilate(resultMat, resultMat, k)
            //_, bw = cv2.threshold(gray, thrsh, 255, cv2.THRESH_OTSU+cv2.THRESH_BINARY_INV)

//kerSz = 11
//k = np.ones((kerSz, kerSz), np.uint8)
//bw = cv2.erode(bw, k) #cv2.dilate(bw, k)#, iterations=2)

        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
