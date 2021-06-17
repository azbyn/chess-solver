package com.azbyn.chess_solver.crop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.*
import kotlinx.android.synthetic.main.crop.*
import org.json.JSONObject

class CropFragment : ImageViewFragment() {
    override val nextFragment = FragmentIndex.ACCEPT
    override val prevFragment = FragmentIndex.ACCEPT

    override fun getImageView(): ImageView = imageView
    private val viewModel: AcceptFragment.VM by viewModelDelegate()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.crop, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back.setOnClickListener { onBack() }
        reset.setOnClickListener {
            imageView.resetZoom()
            overlay.reset()
        }
        ok.setOnClickListener { onOK() }

        //rotateViewer.overlay = overlay
        //overlay.rotateViewer = rotateViewer
    }

    override fun saveData(path: String): JSONObject? = null
    override fun fastForward() = false//Unit

    @SuppressLint("MissingSuperCall")
    override fun onOK() {
        viewModel.crop(mainActivity, overlay.roi)
//        super.onOK()

        // goto accept fragment with isOnBack=true
        onBack()
    }

    override fun initImpl(isOnBack: Boolean) {
        setImagePreview(viewModel.resultMat)
        imageView.resetZoom()
        imageView.runWhenInitialized {
            overlay.init(viewModel.resultMat.width(), viewModel.resultMat.height(), imageView)
            //rotateViewer!!.reset()
        }
    }

    override fun lightCleanup() = overlay.cleanup()
}