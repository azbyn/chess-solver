package com.azbyn.chess_solver.classification

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.svm.OneVsOneSvm
import com.azbyn.chess_solver.svm.OpenCvSvm
import kotlinx.android.synthetic.main.categorise.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.opencv.android.Utils
import org.opencv.core.Core.extractChannel
import org.opencv.core.CvType.CV_8UC4
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc.rectangle
import java.io.File


//typealias MultiSvmKind = OneVsOneSvm
typealias MultiSvmKind = OpenCvSvm

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
    //override val topBarName: String get() = "Categorise"

    class VM : BaseViewModel() {

        private lateinit var boardClassifier: BoardClassifier<MultiSvmKind>
        private val inViewModel: SquaresPreviewFragment.VM by viewModelDelegate()

        private val boardImage get() = inViewModel.boardImage
//        private val previewMat = Mat.zeros(boardSize, boardSize, CV_8UC3)
        private var previewMat = Mat.zeros(boardSize, boardSize, CV_8UC4)


        private var piecesMap = mapOf<Piece, Mat>()

        lateinit var result: Board
//        val result = Array(8) {Array(8) { Piece.Nothing }}

        fun fastForward(frag: BaseFragment) {
            init(frag)
            update()
        }

        fun readPieceClassifier(ctx: MainActivity, relpath: String): PieceClassifier<OpenCvSvm> {
            val inStream = ctx.assets.open(relpath)
            val file = File("${ctx.path}/$relpath")
            file.outputStream().use { inStream.copyTo(it) }

            return pieceClassifierFromFile(file.path)
        }
        val suffix = "_$svmSquareSize"+ (if (svmUseMargins) "_wM" else "_noM")

        private fun readBoard(ctx: MainActivity): BoardClassifier<MultiSvmKind> {
            val str = ctx.assets.open("board$suffix").reader().readText()
//            val str = File("${ctx.path}/$relpath/board$suffix").readText()
            return Json.decodeFromString<BoardClassifier<MultiSvmKind>>(string = str)
        }

        private fun initClassifier(ctx: MainActivity) {
//            boardClassifier = readBoard(ctx)

            boardClassifier = BoardClassifier(
                white=readPieceClassifier(ctx, "white_classifier$suffix"),
                black=readPieceClassifier(ctx, "black_classifier$suffix"))

        }

        private fun initPieces(ctx: MainActivity) {
            initClassifier(ctx)

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
                Piece(Piece.Color.Black, Piece.Type.King)   to impl("pc_bd"),
                Piece(Piece.Color.Black, Piece.Type.Queen)  to impl("pc_qd"),
                Piece(Piece.Color.Black, Piece.Type.Rook)   to impl("pc_rd"),
                Piece(Piece.Color.Black, Piece.Type.Bishop) to impl("pc_bd"),
                Piece(Piece.Color.Black, Piece.Type.Knight) to impl("pc_nd"),
                Piece(Piece.Color.Black, Piece.Type.Pawn)   to impl("pc_pd"),

                Piece(Piece.Color.White, Piece.Type.King)   to impl("pc_bl"),
                Piece(Piece.Color.White, Piece.Type.Queen)  to impl("pc_ql"),
                Piece(Piece.Color.White, Piece.Type.Rook)   to impl("pc_rl"),
                Piece(Piece.Color.White, Piece.Type.Bishop) to impl("pc_bl"),
                Piece(Piece.Color.White, Piece.Type.Knight) to impl("pc_nl"),
                Piece(Piece.Color.White, Piece.Type.Pawn)   to impl("pc_pl")
                )
        }

        override fun init(frag: BaseFragment) {
            frag.tryOrComplain {
                if (piecesMap.isEmpty())
                    initPieces(frag.mainActivity)

                result = boardClassifier.classify(boardImage)
                logd("board:\n$result")
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
                frag.setImageGrayscalePreview(boardImage.mat)
                frag.setImageGrayscalePreview(previewMat, frag.previewImg)
            }
        }

        private fun update() {
            for ((i, j) in result.indices) {
                val col = if ((i+j) % 2 == 0) whiteSquareCol else blackSquareCol
                val x0 = i * squareSize
                val y0 = j * squareSize

                val rect = Rect(x0, y0, squareSize, squareSize)

                rectangle(previewMat, rect, col, -1)
                val piece = result[i, j]
                if (piece.isNothing) continue


                val img = piecesMap[piece]!!
                val mask = Mat()
                extractChannel(img, mask, 3)//extract alpha
                img.copyTo(previewMat.submat(rect), mask)
           }
        }
    }
}
