package com.azbyn.chess_solver.step1

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.*
import kotlinx.android.synthetic.main.edit_square.*
import org.json.JSONObject
import org.opencv.core.Point

class EditSquareFragment : ImageViewFragment() {
    //TODO perspective and partial edit_square, and pruning
    val viewModel: VM by viewModelDelegate()

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

        viewModel.init(this, isOnBack)

        if (isOnBack) {
            update()
        } else {
            onReset()
        }
    }


    override fun saveData(path: String): JSONObject? = null// JSONObject()
    override fun fastForward() = viewModel.fastForward(this)


    private fun onReset() {
        imageView?.resetZoom()
        viewModel.reset(this)
        overlay.reset()
    }
    fun update() {
        viewModel.update(this)
    }

    class VM : BaseViewModel() {
        private val inViewModel: FindBoardFragment.VM by viewModelDelegate()
        private val origMat get() = getViewModel<AcceptFragment.VM>().resultMat

        private val inQuad get() = inViewModel.resultQuad
        val width get() = origMat.width()
        val height get() = origMat.height()

        val points = Array(4) { Point() }
        private var dirty = true

        fun init(frag: BaseFragment, isOnBack: Boolean) {
            super.init(frag)
            dirty = !isOnBack
        }

        fun reset(frag: ImageViewFragment) {
            dirty = true
            update(frag)
        }

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

        fun update(ctx: Context, isFastForward: Boolean = false) {
            logd("update: d = $dirty")
            if (!dirty) return
            dirty = false

            if (inQuad != null) {
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
                //could be compressed with a for, but this is more redable
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
            frag.tryOrComplain {
                logTimeSec { update(frag.mainActivity) }
                frag.setImagePreview(origMat)
            }
        }
    }
}
