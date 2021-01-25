package com.azbyn.chess_solver.step2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.azbyn.chess_solver.*
import kotlinx.android.synthetic.main.categorise.*
import org.json.JSONObject
import org.opencv.android.Utils
import org.opencv.core.Core.extractChannel
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.CvType.CV_8UC4
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc.rectangle

enum class PieceType {
    Nothing,

    King,
    Queen,
    Rook,
    Pawn,
    Bishop,
    Knight,
}
enum class PieceColor { White, Black }
data class Piece(val col: PieceColor, val type: PieceType) {
    constructor() : this(PieceColor.White, PieceType.Nothing)

    val isNothing get() = type == PieceType.Nothing

    companion object {
        var Nothing = Piece(PieceColor.White, PieceType.Nothing)
    }
}

class CategoriseFragment : ImageViewFragment() {
    override fun getImageView(): ZoomableImageView = imageView!!

    val previewImg: ZoomableImageView get() = preview

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.categorise, container, false)

    private val viewModel: VM by viewModelDelegate()

    override fun saveData(path: String): JSONObject? {
        return viewModel.saveData()
    }
    override fun initImpl(isOnBack: Boolean) {
        viewModel.init(this)
        imageView.resetZoom()
        previewImg.resetZoom()
        viewModel.update(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //logd("Accept.onViewCreated")
        back.setOnClickListener { onBack() }
        ok.setOnClickListener { onOK() }
        reset.setOnClickListener {
            viewModel.onReset()
            imageView.resetZoom()
            previewImg.resetZoom()
        }
    }

    override fun fastForward(): Boolean {
        viewModel.fastForward(this)
        return false
    }

    //override val prevFragment = FragmentIndex.PERSPECTIVE
//    override val topBarName: String get() = "Categorise"

    class VM : BaseViewModel() {
        private val inViewModel: OrientationFragment.VM by viewModelDelegate()

        private val mainMat get() = inViewModel.resultMat
//        private val previewMat = Mat.zeros(boardSize, boardSize, CV_8UC3)
        private var previewMat = Mat.zeros(boardSize, boardSize, CV_8UC4)


        private var piecesMap = mapOf<Piece, Mat>()

        val result = Array(64) { _ -> Piece.Nothing }.also {
            it[0] = Piece(PieceColor.White, PieceType.Bishop)
        }

        fun fastForward(frag: BaseFragment) {
            init(frag)
            update()
        }

        private fun initPieces(ctx: Context) {
            fun impl(id: String): Mat {
                val uri = Uri.parse("android.resource://${ctx.packageName}/drawable/$id")
                val stream = ctx.contentResolver.openInputStream(uri)

                val bmpFactoryOptions = BitmapFactory.Options()
                bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888

                val bmp = BitmapFactory.decodeStream(stream, null, bmpFactoryOptions)
                val res = Mat()
                Utils.bitmapToMat(bmp, res)
                return res
            }

            piecesMap = mapOf<Piece, Mat>(
                Piece(PieceColor.Black, PieceType.King)   to impl("pc_bd"),
                Piece(PieceColor.Black, PieceType.Queen)  to impl("pc_qd"),
                Piece(PieceColor.Black, PieceType.Rook)   to impl("pc_rd"),
                Piece(PieceColor.Black, PieceType.Bishop) to impl("pc_bd"),
                Piece(PieceColor.Black, PieceType.Knight) to impl("pc_nd"),
                Piece(PieceColor.Black, PieceType.Pawn)   to impl("pc_pd"),

                Piece(PieceColor.White, PieceType.King)   to impl("pc_bl"),
                Piece(PieceColor.White, PieceType.Queen)  to impl("pc_ql"),
                Piece(PieceColor.White, PieceType.Rook)   to impl("pc_rl"),
                Piece(PieceColor.White, PieceType.Bishop) to impl("pc_bl"),
                Piece(PieceColor.White, PieceType.Knight) to impl("pc_nl"),
                Piece(PieceColor.White, PieceType.Pawn)   to impl("pc_pl")
                )
        }

        override fun init(frag: BaseFragment) {
            frag.tryOrComplain {
                if (piecesMap.isEmpty())
                    initPieces(frag.mainActivity)
            }
        }

        private companion object {
            const val squareSize = 250
            const val boardSize = squareSize * 8
            val whiteSquareCol = Colors.fromRGB(0xc3ac8c)
            val blackSquareCol = Colors.fromRGB(0x3a1d17)
        }

        fun onReset() {
            //TODO
        }

        fun saveData() = null
//        JSONObject().apply {
//            put("history", history)
//        }

        fun drawTo(frag: ResultFragment) {
            frag.tryOrComplain {
//                update()
                frag.setImageGrayscalePreview(previewMat)
            }
        }

        fun update(frag: CategoriseFragment) {
            frag.tryOrComplain {
                update()
                frag.setImageGrayscalePreview(mainMat)
                frag.setImageGrayscalePreview(previewMat, frag.previewImg)
            }
        }

        private fun update() {
            for (i in 0.until(8)) {
                for (j in 0.until(8)) {
                    val col = if ((i+j) % 2 == 0) whiteSquareCol else blackSquareCol
                    val x0 = i * squareSize
                    val y0 = j * squareSize

                    val rect = Rect(x0, y0, squareSize, squareSize)

                    rectangle(previewMat, rect, col, -1)
                    val piece = result[i+j*8]
                    if (piece.isNothing) continue


                    val img = piecesMap[piece]!!
                    val mask = Mat()
                    extractChannel(img, mask, 3)//extract alpha
                    img.copyTo(previewMat.submat(rect), mask)
                }
           }

        }
    }
}
