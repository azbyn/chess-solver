package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.*

class BlurFragment : BaseSlidersFragment(
    SliderData("Blur", default=7, min=1, max=91, stepSize=2),
    //SliderData("Dilate", default=1, min=1, max=21, stepSize=2)) {
    SliderData("isMed", default=1, min=0, max=1, stepSize=1)
) {
    override val prevFragment = FragmentIndex.ACCEPT
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = mainActivity.getString(R.string.blur)

    class VM : SlidersViewModel() {
        //private val inViewModel: SelectRoiFragment.VM by viewModelDelegate()
        private val inViewModel: AcceptFragment.VM by viewModelDelegate()
        private val baseMat get() = inViewModel.resultMat
        var resultMat = Mat()
            private set



        fun redo(blurIncrease: Int, isFastForward: Boolean=true) {
            lastValues[0] += blurIncrease
            logd("redo blur: ${lastValues[0]}")
            update(lastValues, isFastForward)
        }

        private fun updateImpl(baseMat: Mat, resultMat: Mat, blurVal: Int, isMedian: Boolean) {
//            val blurVal = AcceptFragment.VM.convertLength(blurVal_) and 1
            if (isMedian) {
                medianBlur(baseMat, resultMat, blurVal)
            } else {
//                blur(baseMat, resultMat, Size(blurVal.toDouble(), blurVal.toDouble()))
                GaussianBlur(baseMat, resultMat, Size(blurVal.toDouble(), blurVal.toDouble()),0.0)
            }
        }

        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
            updateImpl(baseMat, resultMat, args[0], args[1]!=0)
        }

        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                logTimeSec { update(p) }
                frag.setImageGrayscalePreview(resultMat)
            }
        }
    }
}
