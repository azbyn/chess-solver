package com.azbyn.chess_solver


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.ColorSpace
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.azbyn.chess_solver.Misc.logeImpl
import org.opencv.core.Scalar
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.system.measureTimeMillis

typealias CvRect = org.opencv.core.Rect

//val DESIRED_DENSITY = JniImpl.getDesiredDensity()
const val GRAYED_OUT_COLOR: Int = 0xA0_00_00_00.toInt()
const val IMAGE_FILE_NAME = "img.png"
//shouldn't really matter
val TIME_LOCALE: Locale = Locale.GERMANY

fun Float.format(digits: Int=2) = "%.${digits}f".format(this)
fun Double.format(digits: Int=2) = "%.${digits}f".format(this)
fun Double.toRad() = this / 180 * PI
fun Int.toRad() = this * PI / 180
fun Double.toDeg() = this * 180 / PI

fun <T> toStrImpl(it: Iterator<T>): String {
    var s = "["
    for (e in it)  { s += "$e, " }
    return "$s]"
}
fun IntArray.toStr() = toStrImpl(iterator())
fun DoubleArray.toStr() = toStrImpl(iterator())
fun <T> Array<T>.toStr() = toStrImpl(iterator())
fun <T> Iterable<T>.toStr() = toStrImpl(iterator())

object Colors {
    val green = Scalar(0.0, 255.0, 0.0)
    val red = Scalar(255.0, 0.0, 0.0)
    val magenta = Scalar(255.0, 0.0, 255.0)
    val cyan = Scalar(0.0, 255.0, 255.0)
    val yellow = Scalar(255.0, 255.0, 0.0)
    val blue = Scalar(0.0, 0.0, 255.0)
    val black = Scalar(0.0, 0.0, 0.0)
    val white = Scalar(255.0, 255.0, 255.0)
    val gray = Scalar(128.0, 128.0, 128.0)

    val darkGreen = Scalar(0.0, 128.0, 0.0)
    val darkRed   = Scalar(128.0, 0.0, 0.0)
    val darkMagenta = Scalar(128.0, 0.0, 128.0)
    val darkCyan = Scalar(0.0, 128.0, 128.0)
    val darkYellow = Scalar(128.0, 128.0, 0.0)
    val darkBlue = Scalar(0.0, 0.0, 128.0)

    val niceColors = arrayOf(
        green, red, blue, magenta, cyan, yellow, darkGreen, darkMagenta
    )
    fun getNiceColor(i: Int) = niceColors[i % niceColors.size]
    fun fromRGB(hex: Int): Scalar {
        val r = (hex shr 16 and 0xff).toDouble()
        val g = (hex shr 8 and 0xff).toDouble()
        val b = (hex and 0xff).toDouble()
        return Scalar(r, g, b)
    }
}
object Misc {
    private const val TAG = "azbyn-chess"

    private val formater = SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY)

    fun fmtMsg(priority: String, msg: String, className: String, funName: String): String =
        "${formater.format(Calendar.getInstance().time)} $priority @$className.$funName: $msg"
    fun fmtMsg(priority: String, msg: String, offset: Int): String {
        val st = Thread.currentThread().stackTrace[5+offset]
        val className = st.className.substringAfterLast('.')
        return fmtMsg(priority, msg, className, st.methodName)
    }

    //TODO write to a log file

    fun logi(msg: String = "", e: Throwable? = null, offset: Int=0) =
        Log.i(TAG, fmtMsg("I", msg, offset), e)
    fun logd(msg: String = "", e: Throwable? = null, offset: Int=0) =
        Log.d(TAG, fmtMsg("D", msg, offset), e)

    fun logw(msg: String = "", e: Throwable? = null, offset: Int=0) =
        Log.w(TAG, fmtMsg("W", msg, offset), e)
    fun whyIsThisCalled() {
        val st = Thread.currentThread().stackTrace
        for (i in 3 until st.size) {
            val el = st[i]
            Log.i(TAG, "${el.className}.${el.methodName}@${el.lineNumber}")
        }
    }

    fun logwtf(msg: String, e: Throwable? = null, offset: Int=0) =
        Log.wtf(TAG, fmtMsg("WTF", msg, offset), e)

    fun logeImpl(ctx: Context, msg: String = "", e: Throwable? = null, offset: Int) {
        val st = e?.stackTrace?.get(0) ?: Thread.currentThread().stackTrace[5+offset]
        val className = st.className.substringAfterLast('.')
        val funName = st.methodName

        val stackTraceMsg = if (e != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            sw.toString()
        } else {
            ""
        }
        Log.e(TAG, fmtMsg("E", "", className, funName), e)
        AlertDialog.Builder(ctx)
            .setTitle("Error @$className.$funName")
            .setMessage("$msg: $stackTraceMsg")
            .setPositiveButton(android.R.string.ok) { _, _ -> Unit }//ctx.finish()
            .show()
    }
    fun logeSimple(msg: String = "", e: Throwable? = null) = Log.e(TAG, fmtMsg("E", msg, offset=0), e)
}
inline fun BaseFragment.tryOrComplain(f: () -> Unit) {
    try {
        f()
    } catch (e: Throwable) {
        loge(e)
    }
//fun BaseFragment.loge(e: Throwable?) = logeImpl(mainActivity, "", e)
}

fun BaseFragment.loge(e: Throwable?) = logeImpl(mainActivity, "", e, 0)
fun BaseFragment.loge(msg: String, offset: Int=0) =
    logeImpl(mainActivity, msg, null, offset)

//fun loge(ctx: Context, e: Throwable?) = logeImpl(ctx, "", e)
//fun loge(ctx: Context, msg: String) = logeImpl(ctx, msg, null)

inline fun Context.tryOrComplain(f: () -> Unit) {
    try {
        f()
    } catch (e: Throwable) {
        logeImpl(this, "", e, offset=0)
    }
}
inline fun measureTimeSec(f: () -> Unit): Float = measureTimeMillis(f) / 1000f

fun View.runWhenInitialized(f: (() -> Unit)) {
    if (width != 0) {
        f()
        return
    }
    this.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (width != 0) {
                    f()
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
}
