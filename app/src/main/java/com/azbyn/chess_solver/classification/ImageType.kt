package com.azbyn.chess_solver.classification

enum class MarginType {
    NoMargin,
    UseMargin,
    ;
    val bool get() = this == UseMargin
    companion object {
        val WithMargin get() = UseMargin
    }
}


data class ImageType(val squareSize: Int,
                     val useMargins: MarginType
) {
    val suffix get() = "_$squareSize"+ (if (useMargins.bool) "_wM" else "_noM")
}