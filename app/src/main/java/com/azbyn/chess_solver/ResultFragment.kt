package com.azbyn.chess_solver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.capture.CaptureFragment
import com.azbyn.chess_solver.classification.CategoriseFragment
import kotlinx.android.synthetic.main.result.*
import kotlinx.android.synthetic.main.result.back
import kotlinx.android.synthetic.main.result.imageView
import org.json.JSONObject
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

class ResultFragment : ImageViewFragment() {
    override val nextFragment = FragmentIndex.CAPTURE
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.result, container, false)

    override fun getImageView(): ImageView = imageView

    private val viewModel: CategoriseFragment.VM by viewModelDelegate()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back.setOnClickListener { onBack() }
        newPhoto.setOnClickListener { onOK() }
        wasGood.setOnClickListener {
            wasGood.isChecked = !wasGood.isChecked
        }
        save.setOnClickListener { onSave() }
    }

    override fun initImpl(isOnBack: Boolean) {
        wasGood.isChecked = true
        imageView.resetZoom()
        viewModel.drawTo(this)
    }

    override fun saveData(path: String) = JSONObject().apply {
        put("good", wasGood.isChecked)
    }

    private fun onSave() {
        val t = measureTimeSec {
            tryOrComplain {
//                Imgcodecs.imwrite("${mainActivity.path}/result.png",
//                   viewModel.previewMat)

//                logd("wrote '${mainActivity.path}/result.png'")


                val cf = getViewModel<CaptureFragment.VM>()
                val timestamp = cf.timestamp
                val dir = File(mainActivity.path, timestamp)
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        loge("mkdir failed for ${dir.path}")
                        return
                    }
                }
                val json = JSONObject().apply {
                    put("version", BuildConfig.VERSION_NAME)
                    put("versionCode", BuildConfig.VERSION_CODE)
                    put("buildType", BuildConfig.BUILD_TYPE)
                    put("timestamp", timestamp)
                    put("now", cf.timestampNow())
                }
                val path = dir.path
                logd("path: {$path}")
                for (i in FragmentIndex.values) {
                    val f = fragmentManager.getItem(i)
                    f.mainActivity = mainActivity
                    val obj = f.saveData(path)
                    if (obj != null) {
                        json.put(f.className, obj)
                    }
                }
                File(dir, "data.json").writeText(json.toString(4))
                logd("$timestamp ${json.toString(4)}")
                logd("path: {$path}")
                cf.saveLast(mainActivity)
            }
        }
        logd("saved in ${t}s -")
        showToast("saved in ${t}s")
    }
}
