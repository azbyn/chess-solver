package com.azbyn.chess_solver

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.capture.CaptureFragment
import kotlinx.android.synthetic.main.accept.*
import org.json.JSONObject
import org.opencv.core.CvType.CV_64FC1
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs.imwrite
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AcceptFragment : ImageViewFragment() {
    override val prevFragment = FragmentIndex.CAPTURE
    override val nextFragment = FragmentIndex.BLUR
    //override val nextFragment = FragmentIndex.RESULT

    override fun getImageView(): ImageView = imageView

    private val viewModel: VM by viewModelDelegate()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.accept, container, false)

    override fun saveData(path: String): JSONObject? {
        imwrite("$path/$IMAGE_FILE_NAME", viewModel.resultMat)
        imwrite("$path/og-$IMAGE_FILE_NAME", getViewModel<CaptureFragment.VM>().mat)
        return viewModel.saveData()
    }

    override fun onOK() {
        viewModel.onOK()
        super.onOK()
    }

    override fun initImpl(isOnBack: Boolean) {
        viewModel.init(this)
        imageView.resetZoom()
        setImagePreview(viewModel.resultMat)
    }

    override fun fastForward() = false//Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //logd("Accept.onViewCreated")
        back.setOnClickListener { onBack() }
        rotate.setOnClickListener { setCurrent(FragmentIndex.ROTATE) }
        crop.setOnClickListener { setCurrent(FragmentIndex.CROP) }
        ok.setOnClickListener { onOK() }
        fastForward.setOnClickListener {
            //fragmentManager.fastForwardTo(FragmentIndex.PERSPECTIVE)// .PROBABILISTIC_LINE_MERGE)
            fragmentManager.fastForwardTo(FragmentIndex.EDIT_SQUARE)
        }
        feelingLucky.setOnClickListener {
            //fragmentManager.fastForwardTo(FragmentIndex.FINALIZE_GRID, msg="Felt lucky for")
            fragmentManager.fastForwardTo(FragmentIndex.FEELING_LUCKY, msg="Felt lucky for")
        }
    }


    class VM : BaseViewModel() {
        override fun logd(s: String) = Unit

        //val resultMat get() = getViewModel<CaptureFragment.VM>().mat
        val bigMat get() = getViewModel<CaptureFragment.VM>().mat

        val resultMat = Mat() //get() =

        companion object {
            val ratio = .5
            fun convertLength(l: Int) = (l* ratio).toInt()
            fun convertLength(l: Double) = l* ratio

            fun convertArea(l: Int) = (l* ratio*ratio).toInt()
            fun convertArea(l: Double) = l* ratio* ratio
//            fun convertAreaBackwards(l: Double) = l/ (ratio* ratio)


        }

        override fun init(frag: BaseFragment) {
            super.init(frag)
            resize(bigMat, resultMat, Size(), ratio, ratio)
        }


        private val history = arrayListOf<String>()
        private var changedTimestamp = false
        fun clearHistory() {
            logd("cleared")
            history.clear()
        }
        fun onOK() {
            logd("changed: $changedTimestamp")
            if (!changedTimestamp && history.size != 0) {
                getViewModel<CaptureFragment.VM>().setTimeStampNow()
                changedTimestamp = true
            }
        }
        fun saveData() = JSONObject().apply {
            put("history", history)
        }

        fun crop(ctx: Context, roi: CvRect) {
            ctx.tryOrComplain {
                history.add("Crop $roi")
                logd("$history")
                resultMat.submat(roi).copyTo(resultMat)
            }
        }
        fun rotate(ctx: Context, angle: Double, isHorizontal: Boolean) {
            if (angle == 0.0) return
            ctx.tryOrComplain {
                history.add("rotate: $angle")
                logd("$history")
                val w = resultMat.width().toDouble()
                val h = resultMat.height().toDouble()
                if (isHorizontal) {
                    // More info about the math involved:
                    // https://docs.opencv.org/4.1.0/d4/d61/tutorial_warp_affine.html
                    val m = Mat(2, 3, CV_64FC1)

                    val a = -angle * PI / 180
                    val c = cos(a)
                    val s = sin(a)
                    // this makes it that m*(w/2, h/2, 1) = (h/2, w/2)^t
                    // and that the image gets rotated by the angle
                    m.put(0, 0,
                        +c, s, (1-s)*(h/2) - c * (w/2),
                        -s, c, (1+s)*(w/2) - c * (h/2))

                    warpAffine(resultMat, resultMat, m, Size(h, w))
                } else {
                    //logd("angle $angle")
                    val m = getRotationMatrix2D(Point(w / 2, h / 2), -angle, 1.0)
                    warpAffine(resultMat, resultMat, m, resultMat.size()) // Size(w, h))
                }
            }
        }
    }
}
