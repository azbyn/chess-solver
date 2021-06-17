package com.azbyn.chess_solver.capture

import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.capture.*
import org.json.JSONObject
import org.opencv.core.Core.*
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE
import org.opencv.imgcodecs.Imgcodecs.imread
import java.io.File
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*
import android.view.*
import android.widget.ArrayAdapter
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.Misc.logi
import com.quickbirdstudios.yuv2mat.Yuv
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.cvtColor

class CaptureFragment : CaptureFragmentBase() {
    @Suppress("UNUSED_PARAMETER")
    private fun logd(s: String) = Unit
    //private fun logd(s: String) = Misc.logd(s, offset = 1)

    class VM : DumbViewModel() {
        var mat = Mat()
            private set

//        var ogMat = Mat()
//            private set
        var timestamp = ""
            private set
        private val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", TIME_LOCALE)
        //private var lastFile: File? = null
        //var flashEnabled = false//true

        @Suppress("UNUSED_PARAMETER")
        fun init(mainActivity: MainActivity): Boolean {
            logd("init")
            //lastFile = null
            /*val f = File(mainActivity.path, "last.txt")
            //logd("exists ${f.exists()}")
            if (!f.exists()) return false
            val dirPath = f.readText().trim()
            val dirFile = File(mainActivity.path, dirPath)
            //logd("path '$dirFile'")
            //logd("exists2 ${dirFile.exists()}")
            if (!dirFile.exists()) return false
            lastFile = dirFile
            logd("true: $lastFile")
             */
            return true
        }

        @Suppress("UNUSED_PARAMETER")
        fun saveLast(mainActivity: MainActivity) {
            //lastFile = File("${mainActivity.path}/$timestamp", IMAGE_FILE_NAME)
            //logd("Oida, saved $lastFile")
            /*
            val f = File(mainActivity.path, "last.txt")
            f.writeText("$timestamp/$IMAGE_FILE_NAME")*/
        }
        fun timestampNow(): String = formatter.format(Calendar.getInstance().time)
        fun setTimeStampNow() {
            timestamp = timestampNow()
        }

        fun fromImage(img: Image, rotation: Int) {
            setTimeStampNow()

            // this is very fast but it's not true grayscale (1/3 R, 1/3 G, 1/3 B)
            // but is (0.299 R, 0.578 G, 0.114 B)
            // for our purposes this is fine

            val buffer = img.planes[0].buffer
            val data = ByteArray(buffer.capacity())
            buffer.get(data)
            mat = Yuv.rgb(img)

            cvtColor(mat, mat, COLOR_BGR2GRAY)

//
//            @Suppress("LiftReturnOrAssignment")
//            if (false) {
//                mat = Mat(img.height, img.width, IMREAD_GRAYSCALE) //data)
//                mat.put(0, 0, data)
//            } else {
//                mat = Yuv.rgb(img)
//                cvtColor(mat, mat, COLOR_BGR2GRAY)
//            }
//

            img.close()
            when (rotation) {
                90 -> rotate(mat, mat, ROTATE_90_CLOCKWISE)
                180 -> rotate(mat, mat, ROTATE_180)
                270 -> rotate(mat, mat, ROTATE_90_COUNTERCLOCKWISE)
            }
        }

        fun fromPath(frag: CaptureFragment): Boolean {
            val str = frag.getSelectedString()
            var path = "${frag.mainActivity.path}/$str"
            val idx = str.indexOf('/')
            this.timestamp = (if (idx >= 0) str.substring(0, idx).also { logd("it: $it") } else "")

            val path2 = path.replace("img.png", "og-img.png")
            if (File(path2).exists()) {
                logd("we can use '$path2'")
                path = path2
            }
            mat = imread(path, IMREAD_GRAYSCALE)
            logd("path: $path - timestamp: '$timestamp', sz= ${mat.size()}")
            return true
        }

        fun reset() {
            mat = Mat()//?
        }
    }

    override fun onOK() {
        getViewModel<AcceptFragment.VM>().clearHistory()
        super.onOK()
    }

    override val nextFragment = FragmentIndex.ACCEPT

    private val viewModel: VM by viewModelDelegate()

    private var toast: Toast? = null
    private var time: Long = 0

    override fun saveData(path: String): JSONObject? = null

    private var files = arrayListOf<String>()
    private fun getFiles(): ArrayList<String> {
        val dir = File(mainActivity.path)
        val subFiles = dir.listFiles()
        val res = arrayListOf<String>()
        subFiles ?: return res
        for (f in subFiles) {
            if (f.name.endsWith(".png") || f.name.endsWith(".jpg")) {
                res.add(f.name)
            } else if (f.isDirectory) {
                res.add("${f.name}/img.png")
            }
        }
        res.sort()
        return res
    }


    override fun onCreateView(i: LayoutInflater, container: ViewGroup?, b: Bundle?): View?
            = i.inflate(R.layout.capture, container, false)

    override fun onImageAvailable(it: ImageReader) {
        logd("onImageAvailable $mainActivity")
        tryOrComplain {
            val timeBegin = currentTimeMillis()
            viewModel.fromImage(it.acquireNextImage(), orientation)
            logd("orientation: $orientation")
            toast?.cancel()
            val now = currentTimeMillis()
            val dt = (now - time) / 1e3f
            val beginDt = (now - timeBegin) / 1e3f

            logd("time $dt (in listener $beginDt)")
            showToast("Done $dt")
            mainActivity.runOnUiThread { onOK() }
        }
    }

    private fun setUseSavedColor(value: Boolean) {
        fun impl(id: Int) {
            val color = ContextCompat.getColor(mainActivity, id)
            val colorTint = ContextCompat.getColorStateList(mainActivity, id)
            useSaved.backgroundTintList = colorTint
            useSaved.setTextColor(color)
        }
        impl(if (value) R.color.default_ else R.color.grayedOut)
    }
    fun getSelectedString() :String {
        return dropdown.selectedItem as String
    }

    override fun init() {
        super.init()
        viewModel.init(mainActivity)
        if (files.isEmpty()) files = getFiles()
        //logi("files: $files")
        val adapter = ArrayAdapter<String>(mainActivity,
            R.layout.support_simple_spinner_dropdown_item,
            files)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = adapter
    }
    override fun initImpl(isOnBack: Boolean) {
        super.initImpl(isOnBack)
        val res = viewModel.init(mainActivity)
        setUseSavedColor(res)
        logd("b: $isOnBack, f: $flashEnabled}")
        /*
        if (isOnBack) {
            flashEnabled = viewModel.flashEnabled
        } else {
            viewModel.flashEnabled = flashEnabled
        }*/
        //flashEnabled = false
        //viewModel.flashEnabled = false
//        if (!isOnBack) {
//            this.toggleFlash()
//        }
        /*flash.setImageResource(
            if (flashEnabled) R.drawable.ic_flash_auto
            else R.drawable.ic_flash_off)*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        picture.setOnClickListener { takePicture() }
        useSaved.setOnClickListener {
            if (!viewModel.fromPath(this)) {
                setUseSavedColor(false)
            } else {
                onOK()
            }
        }
    }

    override fun takePicture() {
        time = currentTimeMillis()
        viewModel.reset()
        toast = Toast.makeText(mainActivity, "Please wait...", Toast.LENGTH_SHORT).also {
            it.show()
        }
        super.takePicture()
    }
}
