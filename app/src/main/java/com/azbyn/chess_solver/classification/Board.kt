package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.MultiSvm
import kotlinx.serialization.Serializable
import org.opencv.core.Mat


//todo addW margin and wo margin?
@Serializable
data class BoardClassifier<T: MultiSvm>(val black: PieceClassifier<T>, val white: PieceClassifier<T>) {
    constructor(black: T, white: T): this(PieceClassifier(black), PieceClassifier(white))
}

class Board(private val values: Array<Piece>) {
    constructor(init: (Int, Int) -> Piece):
            this(List(8*8) { i -> init(i% 8, i /8) }.toTypedArray())
    operator fun get(x: Int, y: Int) = values[y*8+x]
    val indices get() = List(8*8) { i -> Pair(i% 8, i /8) }

    companion object {
        fun isBlackSquare(x: Int, y: Int) = (x + y) % 2 == 1
    }

    override fun toString(): String {
        return (0 until 8).joinToString(separator = "\n") { j-> (0 until 8).joinToString(separator = " ")
        { i -> get(i, j).toString() } }
    }
}

class BoardImage(val mat: Mat, private val xCoords: DoubleArray, private val yCoords: DoubleArray) {
    fun getPieceBounds(x: Int, y: Int) = BoundsD(
        x0 = xCoords[x],
        y0 = yCoords[y],
        x1 = xCoords[x+1],
        y1 = yCoords[y+1])

    fun getVectorAt(x: Int, y: Int) = pieceImageToVector(mat, getPieceBounds(x, y))
}

fun <T: MultiSvm> BoardClassifier<T>.classify(boardImage: BoardImage): Board {
    return Board { x, y ->
        val classifier = if (Board.isBlackSquare(x, y)) this.black else this.white
        val vector = boardImage.getVectorAt(x, y)
        val res = classifier.classify(vector)

        /*
        if (!res.isNothing)
           logd("[$x, $y]: bl ${if (Board.isBlackSquare(x, y)) "b" else "w"} $res")
         */
        return@Board res
    }
}