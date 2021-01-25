package com.azbyn.chess_solver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.Misc.logwtf
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    lateinit var fragmentManager: FragmentManagerAdapter
    val path: String by lazy { getExternalFilesDir(null)!!.path }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryOrComplain {
            setContentView(R.layout.main_activity)
            fragmentManager = FragmentManagerAdapter(supportFragmentManager,
                findViewById(R.id.container), savedInstanceState)
            if (savedInstanceState == null) {
                logd("THE BEGINNING <onCreate>")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        fragmentManager.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() = fragmentManager.onBack()


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main_activity)
//    }
    companion object {
        init {
            if (!OpenCVLoader.initDebug()) {
                logwtf("OpenCV initDebug failed")
            }
        }
    }
}
