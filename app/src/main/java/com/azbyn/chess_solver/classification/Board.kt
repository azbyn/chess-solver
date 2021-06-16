package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.MultiSvm
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import kotlin.system.exitProcess


//todo addW margin and wo margin?
//@Serializable



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

    fun getVectorAt(x: Int, y: Int, imageType: ImageType) =
        pieceImageToVector(mat, getPieceBounds(x, y), imageType)
}
