package com.azbyn.chess_solver.classification

import com.azbyn.chess_solver.svm.MultiSvm
import kotlinx.serialization.Serializable


//todo addW margin and wo margin?
//@Serializable
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