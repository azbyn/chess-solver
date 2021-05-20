package com.azbyn.chess_solver.classification


data class Piece(val col: Color, val type: Type) {
    enum class Color { White, Black }
    enum class Type {
        Nothing,

        King,
        Queen,
        Rook,
        Pawn,
        Bishop,
        Knight,
    }
//    constructor() : this(PieceColor.White, PieceType.Nothing)

    val isNothing get() = type == Type.Nothing

    fun toClass(): Int = type.ordinal + pieceTypeSize * col.ordinal
    override fun toString(): String {
        val func = when (col) {
            Color.White -> Char::toUpperCase
            Color.Black -> Char::toLowerCase
        }
        val c = when (type) {
            Type.Nothing -> '.'
            Type.King -> 'k'
            Type.Queen -> 'q'
            Type.Rook -> 'r'
            Type.Pawn -> 'p'
            Type.Bishop -> 'b'
            Type.Knight -> 'n'
        }
        return func(c).toString()
    }

    companion object {
        val Nothing = Piece(Color.White, Type.Nothing)

        val pieceTypeSize = Type.values().size
        fun fromClass(cl: Int) = Piece(
            Color.values()[cl / pieceTypeSize],
            Type.values()[cl % pieceTypeSize])
    }
}