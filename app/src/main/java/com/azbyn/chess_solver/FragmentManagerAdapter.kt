@file:Suppress("MemberVisibilityCanBePrivate")

package com.azbyn.chess_solver

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.azbyn.chess_solver.Misc.logd
import com.azbyn.chess_solver.capture.CaptureFragment
import com.azbyn.chess_solver.classification.CategoriseFragment
import com.azbyn.chess_solver.classification.SquaresPreviewFragment
import com.azbyn.chess_solver.step1.*
import com.azbyn.chess_solver.step2.*
import com.azbyn.chess_solver.crop.CropFragment
import com.azbyn.chess_solver.rotate.RotateFragment

//import com.azbyn.ocr.crop.CropFragment
//import com.azbyn.ocr.remove_lines.*
//import com.azbyn.ocr.roi.*
//import com.azbyn.chess_solver.rotate.RotateFragment

class NoSwipeViewPager: ViewPager {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean = false
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = false
}

@Suppress("unused")
enum class FragmentIndex(private val clazz: Class<*>) {
    // The order matters, as pressing back defaults to the fragment above in this list
    // and pressing ok defaults to the fragment below
    CAPTURE(CaptureFragment::class.java),
    ACCEPT(AcceptFragment::class.java),
    ROTATE(RotateFragment::class.java),
    CROP(CropFragment::class.java),

//
//    SELECT_ROI(SelectRoiFragment::class.java),
    //BLUR_ROI(BlurRoiFragment::class.java),
//    EDIT_THRESHOLD_MAX(EditThresholdMaxFragment::class.java),
//    EDIT_THRESHOLD_MIN(EditThresholdMinFragment::class.java),
//    GRID_MORPHOLOGICAL(GridMorphologicalFragment::class.java),
//    EDIT_LINES(EditLinesFragment::class.java),
//    SUPER_LINES(SuperLinesFragment::class.java),
//    SELECT_DENSITY(SelectDensityFragment::class.java),
//
//    ACCEPT_DENSITY(AcceptDensityFragment::class.java),
    BLUR(BlurFragment::class.java),
    //THRESHOLD(ThresholdFragment::class.java),
    //MORPHOLOGICAL(MorphologicalFragment::class.java),
    EDGE(EdgeFragment::class.java),
    //LINE(LineFragment::class.java),

    PROBABILISTIC_LINE(ProbabilisticLineFragment::class.java),
    PROBABILISTIC_LINE_MERGE(ProbabilisticLineMergeFragment::class.java),
    CONNECT_SEGMENTS(ConnectSegmentsFragment::class.java),
    FIND_BOARD(FindBoardFragment::class.java),


    EDIT_SQUARE(EditSquareFragment::class.java),
    PERSPECTIVE(PerspectiveFragment::class.java),

    EDGE2(Edge2Fragment::class.java),
    LINE2(Line2Fragment::class.java),

    LINE_MERGE2(LineMerge2Fragment::class.java),



//    DILATE(DilateFragment::class.java),
//    CONTOURS(ContoursFragment::class.java),
//
//    SMALL_SQUARES(SmallSquaresFragment::class.java),
    ORIENTATION(OrientationFragment::class.java),
//
    SQUARES_PREVIEW(SquaresPreviewFragment::class.java),

    CATEGORISE(CategoriseFragment::class.java),

    // TODO cleanup stuff (remove the files also)


    //CONTOURS(ContoursFragment::class.java),
    //SUPER_QUADS(SuperQuadsFragment::class.java),
    //SUPER_QUADS_MERGE(SuperQuadsMergeFragment::class.java),
    //FINALIZE_GRID(FinalizeGridFragment::class.java),

    //LINE(LineFragment::class.java),
//    LINES(LinesFragment::class.java),
//    SUPER_LINES_MK2(SuperLinesMk2Fragment::class.java),
//    LINES_MASK(LinesMaskFragment::class.java),
//    REMOVE_LINES(RemoveLinesFragment::class.java),
//    // end skipped
//    BLOB_MASK1(BlobMask1Fragment::class.java),
//    BLOB_MASK2(BlobMask2Fragment::class.java),
//    BLOBBING(BlobbingFragment::class.java),
//
    RESULT(ResultFragment::class.java)
    ;
    fun newInstance(): BaseFragment = clazz.newInstance() as BaseFragment
    fun prev() : FragmentIndex {
        return if (ordinal == 0) this
        else values[ordinal - 1]
    }
    fun next() : FragmentIndex {
        return if (ordinal == LEN - 1) this
        else values[ordinal + 1]
    }
    companion object {
        val values = values()
        val LEN = values.size
        val FINAL = values[LEN-1]

        val FEELING_LUCKY = RESULT

        val STEP1_END = PERSPECTIVE
        val STEP2_END = ORIENTATION //RESULT.prev()//todo

        private val indexMap = mutableMapOf<Class<*>, FragmentIndex>()
        fun get(clazz: Class<*>): FragmentIndex = indexMap[clazz]!!
        //fun fromInt(int: Int) = values[int]
        init {
            for (i in values) {
                indexMap[i.clazz] = i
            }
        }
    }
}
class FragmentManagerAdapter(
    fm: FragmentManager,
    private val viewPager: NoSwipeViewPager,
    savedInstanceState: Bundle?
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    init {
        viewPager.adapter = this
        if (savedInstanceState != null) {
            val v = savedInstanceState.getInt("curr")
            if (v >= 0 && v <= FragmentIndex.LEN) {
                setCurrent(FragmentIndex.values[v], isOnBack = false)
            }
        }
    }
    companion object {
        private val fragments = Array<BaseFragment?>(FragmentIndex.LEN) { null }
        init {
            for (i in FragmentIndex.values()) {
                fragments[i.ordinal] = i.newInstance()
            }
        }

        fun replaceFragment(index: FragmentIndex, frag: BaseFragment) {
            fragments[index.ordinal] = frag
        }
    }
    fun onSaveInstanceState(outState: Bundle) = outState.putInt("curr", current)

    override fun getItem(position: Int): BaseFragment = fragments[position]!!
    fun getItem(index: FragmentIndex) = getItem(index.ordinal)

    override fun getCount(): Int = FragmentIndex.LEN

    private val currentFragment get() = fragments[viewPager.currentItem]

    private val current: Int get() = viewPager.currentItem
    private val currentIndex get() = FragmentIndex.values[current]

    fun setCurrent(index: FragmentIndex, isOnBack: Boolean) {
        currentFragment?.lightCleanup()
        viewPager.currentItem = index.ordinal
        currentFragment!!.init(isOnBack)
    }
    fun onBack() {
        currentFragment!!.onBack()
    }

    private var pendingFastForward = false

    fun fastForwardFromToImpl(from: FragmentIndex, to: FragmentIndex, msg: String="Done in"): Boolean {
        //logd("pending? $pendingFastForward")
        if (pendingFastForward) return false
        if (from == to) return false
        pendingFastForward = true
        //logd("skip")
        // here the exact fragment doesn't matter,
        // we care just that it's initialised
        val frag = currentFragment!!
        frag.tryOrComplain {
            val t = measureTimeSec {
                var fi: FragmentIndex = currentIndex
                val mainActivity = frag.mainActivity
                while (fi != to) {
                    val f = getItem(fi)
                    f.mainActivity = mainActivity
                    if (f.fastForward()) {
                        pendingFastForward = false
                        setCurrent(fi, isOnBack=false)

                        return true// break
                    }
                    fi = f.nextFragment
                }
            }
            //after()
            logd("$msg ${t}s.")
            frag.showToast("$msg ${t}s.")

        }
        pendingFastForward = false
        return false
        //logd("finish")
    }
    fun fastForwardTo(to: FragmentIndex, msg: String = "Done in") {
        if (fastForwardFromToImpl(currentIndex, to, msg)) return
//        logd("лиза!")

        setCurrent(to, isOnBack=false)
    }
}
