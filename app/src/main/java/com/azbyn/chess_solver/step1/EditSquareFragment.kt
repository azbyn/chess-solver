package com.azbyn.chess_solver.step1

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.Misc.whyIsThisCalled
import kotlinx.android.synthetic.main.edit_square.*
import org.json.JSONObject
import org.opencv.core.Point

class EditSquareFragment : ImageViewFragment() /*BaseSlidersFragment(
    SliderData("reverseRot", default=0, min=0, max=1, stepSize=1)
) */{
    //TODO perspective and partial edit_square, and pruning
    val viewModel: VM by viewModelDelegate()
    //override val topBarName: String get() = "Square"
    override fun onCreateView(i: LayoutInflater, container: ViewGroup?, b: Bundle?): View?
            = i.inflate(R.layout.edit_square, container, false)

    override fun getImageView(): ImageView = imageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back.setOnClickListener { onBack() }
        reset.setOnClickListener { onReset() }
        ok.setOnClickListener { onOK() }

        feelingLucky.setOnClickListener {
            fragmentManager.fastForwardTo(FragmentIndex.FEELING_LUCKY, msg="Felt lucky for")
        }
        fastForward.setOnClickListener {
            fragmentManager.fastForwardTo(FragmentIndex.STEP2_END)
        }
    }

    override fun initImpl(isOnBack: Boolean) {
        imageView?.resetZoom()
        overlay.init(viewModel.width, viewModel.height, imageView, this)
        //logd("$this($isOnBack)")
        //viewModelInit()
        viewModel.init(this, isOnBack)


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


    override fun saveData(path: String): JSONObject? = null// JSONObject()
    override fun fastForward() = viewModel.fastForward(this)


    private fun onReset() {
        imageView?.resetZoom()
//        update()
        viewModel.reset(this)
//        logd("reset()")
        overlay.reset()
    }
    fun update() {
        viewModel.update(this)
    }

    class VM : BaseViewModel() {
        private val inViewModel: /*LineFragment*/FindBoardFragment.VM by viewModelDelegate()
        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        //private val previewMat get() = ogMat// = Mat()
//        private val ogLines get() = inViewModel.goodLines
//        private val angleDeg get() = inViewModel.angleDeg

        private val inQuad get() = inViewModel.resultQuad
        val width get() = ogMat/* previewMat*/.width()
        val height get() = ogMat/*previewMat*/.height()

        val points = Array(4) { Point() }
        private var dirty = true

//        override fun init(frag: BaseFragment) {
//            super.init(frag)
//            dirty = true
//        }
        fun init(frag: BaseFragment, isOnBack: Boolean) {
            super.init(frag)
            dirty = !isOnBack
        }

        fun reset(frag: ImageViewFragment) {
            dirty = true
            update(frag)
        }
            // MatOfPoint2f()//Point[4]
           // private set

        ////////
        fun fastForward(frag: BaseFragment): Boolean {
            val t = measureTimeSec {
                init(frag)
//                dirty = true
                update(frag.mainActivity, isFastForward=true)
                //cleanup()
            }
//            logd("$className: $t")
            //if this is null, then we don't continue the fast forwarding
            return inQuad == null
        }

        ////////

        fun update(ctx: Context, isFastForward: Boolean = false) {
            logd("update: d = $dirty")
            if (!dirty) return
            dirty = false

            if (inQuad != null) {
//                logd("updated properly")
//                whyIsThisCalled()
                for ((i, p) in inQuad!!.points.withIndex()) {
                    this.points[i] = p.clone()
                }
            } else {
                if (!isFastForward) {
                    AlertDialog.Builder(ctx)
                        .setTitle("No board detected")
                        .setMessage("Please manually select the board (or take another picture)")
                        .setPositiveButton(android.R.string.ok) { _, _ -> Unit }
                        .show()
                }
                //could be compressed with a for, but this is more explicit
                this.points[0].x = width/4.0
                this.points[1].x = 3*width/4.0

                this.points[3].x = width/4.0
                this.points[2].x = 3*width/4.0

                this.points[0].y = height/4.0
                this.points[2].y = 3*height/4.0

                this.points[1].y = height/4.0
                this.points[3].y = 3*height/4.0
            }
        }
        fun update(frag: ImageViewFragment) {
//            logd("upd8 ξ $frag")
            frag.tryOrComplain {
                logTimeSec { update(frag.mainActivity) }
//                logd("set preview? ${ogMat.size()}")
                frag.setImagePreview(ogMat)
            }
        }
    }
}
