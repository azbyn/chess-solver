package com.azbyn.chess_solver.rotate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logd
import kotlinx.android.synthetic.main.rotate.*
import org.json.JSONObject

class RotateFragment : ImageViewFragment() {
    override val nextFragment = FragmentIndex.ACCEPT
    override val prevFragment = FragmentIndex.ACCEPT

    override fun getImageView(): ImageView = imageView
    //override val fragmentIndex = FragmentManagerAdapter.ROTATE
    private val viewModel: AcceptFragment.VM by viewModelDelegate()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.rotate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back.setOnClickListener { onBack() }
        reset.setOnClickListener { rotateViewer?.reset() }
        add90.setOnClickListener { rotateViewer?.rotate90() }
        ok.setOnClickListener {
            onOK() }

        rotateViewer.overlay = overlay
        overlay.rotateViewer = rotateViewer
    }

    override fun fastForward() = false//Unit

    override fun saveData(path: String): JSONObject? = null

    override fun initImpl(isOnBack: Boolean) {
        setImagePreview(viewModel.resultMat)

        imageView.runWhenInitialized {
            overlay.initMatrix(viewModel.resultMat.width(), viewModel.resultMat.height(), imageView)
            rotateViewer!!.reset()
        }

        // an ugly way to solve a bug
        imageView.runWhenInitialized {
            overlay.initMatrix(viewModel.resultMat.width(), viewModel.resultMat.height(), imageView)
            rotateViewer!!.reset()
        }
        setImagePreview(viewModel.resultMat)
    }
    @SuppressLint("MissingSuperCall")
    override fun onOK() {
        viewModel.rotate(mainActivity, overlay.angle, overlay.isHorizontal)

        // goto accept fragment with isOnBack=true
        onBack()
    }
}