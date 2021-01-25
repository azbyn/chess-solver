package com.azbyn.chess_solver.step2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logi
import com.azbyn.chess_solver.step1.PerspectiveFragment
import kotlinx.android.synthetic.main.small_squares.*
import org.json.JSONObject
import org.opencv.core.*
import org.opencv.core.CvType.CV_32FC2
import org.opencv.core.CvType.CV_32S
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI


class SmallSquaresFragment : ImageViewFragment() /*BaseSlidersFragment(
    SliderData("reverseRot", default=0, min=0, max=1, stepSize=1)
) */{
    val viewModel: VM by viewModelDelegate()
    //override val topBarName: String get() = "Square"
    override fun onCreateView(i: LayoutInflater, container: ViewGroup?, b: Bundle?): View?
            = i.inflate(R.layout.small_squares, container, false)

    override fun getImageView(): ImageView = imageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.back).setOnClickListener { onBack() }
        view.findViewById<View>(R.id.reset).setOnClickListener { onReset() }
        view.findViewById<View>(R.id.ok).setOnClickListener { onOK() }

    }


    override fun initImpl(isOnBack: Boolean) {
        imageView?.resetZoom()
        overlay.init(viewModel.width, viewModel.height, imageView, this)
        //logd("$this($isOnBack)")
        //viewModelInit()
        viewModel.init(this)


        //initSliderData(sliderDatas)
        /*runIgnoreSeekBar {
            for ((i, s) in sliderDatas.withIndex()) {
                s.setMax(seekBars[i]!!)
            }
        }*/
        if (isOnBack) {
            /*runIgnoreSeekBar {
                for ((i, sd) in sliderDatas.withIndex()) {
                    sd.setProgress(seekBars[i]!!, valueTexts[i]!!, lastValues[i])
                }
            }*/
            update()
        } else {
            onReset()
        }
    }


    override fun saveData(path: String) = viewModel.saveData()
    override fun fastForward() = viewModel.fastForward(this)


    private fun onReset() {
        imageView?.resetZoom()
        update()
    }
    fun update() {
        viewModel.update(this)
    }

    class VM : BaseViewModel() {
        private val inViewModel: ContoursFragment.VM by viewModelDelegate()
        private val mat get() = getViewModel<PerspectiveFragment.VM>().resultMat
        private val dilateAmount get() = getViewModel<DilateFragment.VM>().dilateAmount
        private val contours get() = inViewModel.contours
        //private var previewMat = Mat()

        val width get() = mat.width()
        val height get() = mat.height()

        fun saveData() = JSONObject().apply {
//            put("didRotate", didRotate)
            put("x", rect.x)
            put("y", rect.x)
            put("width", rect.width)
            put("height", rect.height)
        }

        /*var points = Array(4) { Point() }
            // MatOfPoint2f()//Point[4]
            private set
         */
        //val rect = CvRect()
        var rect = CvRect()
            private set


        fun fastForward(frag: BaseFragment): Boolean {
            val t = measureTimeSec {
                init(frag)
                update(isFastForward=true)
                //cleanup()
            }
            logd("$className: $t")
            return false
        }


        fun update(isFastForward: Boolean=false) {
            val minMax = MinMaxRect()
            //val yMinMax = MinMax()

            //val approx2f = MatOfPoint2f()
            //val approx = MatOfPoint()
            //val curr2f = MatOfPoint2f()
            for ((i, c) in contours.withIndex()) {
                minMax.checkContour(c)
                /*c.convertTo(curr2f, CV_32FC2)
                //MatOfPoint2f(*c.toArray())
                val epsilon = .1 * arcLength(curr2f, true)
                approxPolyDP(curr2f, approx2f, epsilon, true)
                val size = curr2f.height()
                //logi("sz: $size")
                /*approx2f.convertTo(approx, CV_32S)
                val area = abs(contourArea(approx))*/
                for (j in 0 until size) {
                    val p = curr2f[j, 0]
                    //logi("pt: $p")
                    xMinMax.check(p[0])
                    yMinMax.check(p[1])
                    //circle(mat, Point(p[0], p[1]), 3,  Scalar(255.0))
                }*/
            }
            //fun pointFromPair(p: Pair<Int, Int>)
            //        = contours[p.first][p.second, 0]

//            fun setPoint(xIdx: Pair<Int, Int>, yIdx: Pair<Int, Int>, result: Point) {
//                result.x = pointFromPair(xIdx)[0]
//                result.y = pointFromPair(yIdx)[1]
//            }

            /*fun setPoint(x: Double, y: Double, result: Point) {
                result.x = x
                result.y = y
            }*/
            if (contours.isEmpty()) {
                rect = CvRect(0,0, mat.width(), mat.height())
            } else {
                val padding = dilateAmount //+3
                minMax.x.min -= padding
                minMax.x.max += padding
                minMax.y.min -= padding
                minMax.y.max += padding

                if (minMax.x.min < 0) minMax.x.min = 0.0
                if (minMax.y.min < 0) minMax.y.min = 0.0

                if (minMax.x.max > mat.width()) minMax.x.max = mat.width().toDouble()
                if (minMax.y.max > mat.height()) minMax.y.max = mat.height().toDouble()

                logd("max; ")
                logd("matThing = ${mat.size()} ")
                logd("x: ${minMax.x.min} vs ${minMax.x.max}")
                logd("y: ${minMax.y.min} vs ${minMax.y.max}")

                rect = minMax.makeRect()
            }


//            xMinMax.min -= padding
//            xMinMax.max += padding
//            yMinMax.min -= padding
//            yMinMax.max += padding
            /*rect.x = xMinMax.min.toInt()
            rect.y = yMinMax.min.toInt()
            rect.width = xMinMax.max.toInt() - rect.x
            rect.height= yMinMax.max.toInt() - rect.y
*/
            /*

            setPoint(xMinMax.min, yMinMax.min, points[0])
            setPoint(xMinMax.min, yMinMax.max, points[1])
            setPoint(xMinMax.max, yMinMax.min, points[2])
            setPoint(xMinMax.max, yMinMax.max, points[3])*/
        }

        fun update(frag: ImageViewFragment) {
            frag.tryOrComplain {
                logTimeSec { update() }
                frag.setImagePreview(mat)
            }
        }
    }
}
