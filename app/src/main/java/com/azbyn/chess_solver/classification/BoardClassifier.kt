package com.azbyn.chess_solver.classification

interface BoardClassifier {
    fun classify(boardImage: BoardImage): Board {
        return Board { x, y ->
            this.classifyAt(x, y, boardImage)
//            val c = if (isBlack) 'B' else 'W'
//        println("$c $x, $y: $choices")
        }
    }
    fun classifyAt(x: Int, y: Int, boardImage: BoardImage): Piece
}

data class PieceBoardClassifier(
    val black: PieceClassifier, val white: PieceClassifier, val imageType: ImageType
):BoardClassifier  {
    //    constructor(black: T, white: T): this(PieceClassifier(black), PieceClassifier(white))
    override fun classifyAt(x: Int, y: Int, boardImage: BoardImage): Piece {
        val isBlack = Board.isBlackSquare(x, y)
        val classifier = if (isBlack) this.black else this.white
        val vector = boardImage.getVectorAt(x, y, imageType = imageType)

        val choices = classifier.classifyChoices(vector)

        val res = choices.first()

        return res.result
    }
}

data class BoardClassifierOnlyIsEmpty(
    val black: IsEmptyClassifier, val white: IsEmptyClassifier, val imageType: ImageType
): BoardClassifier {
    override fun classifyAt(x: Int, y: Int, boardImage: BoardImage): Piece {
        val isBlack = Board.isBlackSquare(x, y)
        val classifier = if (isBlack) this.black else this.white
        val vector = boardImage.getVectorAt(x, y, imageType)

        val res = classifier.isEmptySquare(vector)

        val c = if (isBlack) 'B' else 'W'
        println("$c $x, $y: $res")

        return if (res) Piece.Nothing else Piece(Piece.Color.White, Piece.Type.Pawn)
    }
}

data class DoubleBoardClassifier(
    val white: DoubleClassifier,
    val black: DoubleClassifier,
    val emptyImageType: ImageType,
    val multiImageType: ImageType,
) : BoardClassifier {
    override fun classifyAt(x: Int, y: Int, boardImage: BoardImage): Piece {
        val isBlack = Board.isBlackSquare(x, y)
        val classifier = if (isBlack) this.black else this.white
        val emptyV = boardImage.getVectorAt(x, y, imageType = emptyImageType)
        val multiV = boardImage.getVectorAt(x, y, imageType = multiImageType)

        val res = classifier.classify(emptyV = emptyV, multiV = multiV)

        return Piece.fromClass(res)
    }
}