package com.azbyn.chess_solver.step1

import android.content.Context
import android.util.AttributeSet
import com.azbyn.chess_solver.BaseQuadOverlay
import com.azbyn.chess_solver.ZoomableImageView

class EditSquareOverlay : BaseQuadOverlay {
    lateinit var vm: EditSquareFragment.VM

    override val points get() = vm.points

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    fun init(drawableWidth: Int, drawableHeight: Int, imageView: ZoomableImageView, from: EditSquareFragment) {
        init(drawableWidth, drawableHeight, imageView)
        vm = from.viewModel
    }
}
