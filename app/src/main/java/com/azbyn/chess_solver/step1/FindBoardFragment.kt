package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PointQuad
import com.azbyn.chess_solver.step1.ConnectSegmentsFragment.VM.Connection
import com.azbyn.chess_solver.step1.ConnectSegmentsFragment.VM.SegmentPointIndex
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.COLOR_GRAY2RGB
import org.opencv.imgproc.Imgproc.cvtColor
import kotlin.math.abs
import kotlin.math.min

class FindBoardFragment : BaseSlidersFragment(
    SliderData("i", default=0, min = 0, max = 500, stepSize =1),
    SliderData("angle", default = 15, min = 1, max = 45, stepSize = 1),
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Find board"

    class VM : SlidersViewModel() {
        private val inViewModel: ConnectSegmentsFragment.VM by viewModelDelegate()
        private val connections get() = inViewModel.connections

        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()

        var resultQuad: PointQuad? = null
            private set

        private var remainingIters = 0

        private val defaultRatio = 3.0// 1.2

        override fun init(frag: BaseFragment) {
            super.init(frag)
            remainingIters = 10
            generateQuads()
        }
        class ConnectionQuad(val quad: PointQuad)

        private val connectionQuads = arrayListOf<ConnectionQuad>()
        private fun generateQuads(ratio: Double = defaultRatio) {
            connectionQuads.clear()
            impl(connectionQuads)
            connectionQuads.sortBy {
                abs(it.quad.area() - 2300)
            }
            /*connectionQuads.sortByDescending {
                it.quad.area()
            }*/

            // todo take into account how close the angles are to 90 deg
            // (a trapezoid is more likely to be a board than a parallelogram)
            logd("areas: ${connectionQuads.map { it.quad.area() }}")

            if (connectionQuads.isEmpty()) {
                redo(-10)
            } else {
                val dimensions = connectionQuads[0].quad.dimensions()

                //if it's too elongated, it's probably wrong
                if (dimensions.width * ratio < dimensions.height ||
                    dimensions.height * ratio < dimensions.width) {

                    if (remainingIters <= 0) return
                    --remainingIters

                    inViewModel.redoRadius(5)
                    generateQuads()
                }
            }
        }

        //returns true if it got out of iters
        @Suppress("MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
        fun redo(threshIncrease: Int, isFastForward: Boolean=true, checkIters: Boolean=true): Boolean {
            if (checkIters) {
                if (remainingIters <= 0) return true
                --remainingIters
                logd("iter $remainingIters")
            }

            inViewModel.redo(threshIncrease, isFastForward=true, checkIters=false)
            generateQuads()

            return false
        }

        private fun segmentBuffersToPoints(ci: ArrayList<ConnectionIdx>): ConnectionQuad { // Array<Point> {
            return ConnectionQuad(
                PointQuad { connections[ci[it]].intersection })

        }

        data class ConnectionIdx(val connIdx: Int)
        operator fun ArrayList<Connection>.get(connIdx: ConnectionIdx) = this[connIdx.connIdx]

        private fun impl(arrayOfRes: ArrayList<ConnectionQuad>,
                         level: Int = 0,
                         startConnectionIdx: Int = 0,
                         completedSegmentsIndices: ArrayList<Int> = arrayListOf(),
                         buffer: ArrayList<ConnectionIdx> = arrayListOf()
        ) {
            fun onSuccess(spi: SegmentPointIndex, connIdx: Int, addToCompletedSegments: Boolean) {
                val lastBufferIdx = buffer.size
                val lastCompletedSegmentIdx = completedSegmentsIndices.size
                buffer.add(ConnectionIdx(connIdx))
                if (addToCompletedSegments) {
                    completedSegmentsIndices.add(spi.segIdx)
                }

                if (buffer.size == 4) {

                    val new = segmentBuffersToPoints(buffer)
                    if (!arrayOfRes.contains(new))
                        arrayOfRes.add(new)
                } else {
                    impl(arrayOfRes, level+1,
                        connIdx+1, completedSegmentsIndices, buffer)
                }
                buffer.removeAt(lastBufferIdx)
                if (addToCompletedSegments) {
                    completedSegmentsIndices.removeAt(lastCompletedSegmentIdx)
                }
            }

            for (connIdx in startConnectionIdx until connections.size) {
                val con = connections[connIdx]

                fun verify(spi: SegmentPointIndex): Boolean {
                    if (completedSegmentsIndices.any { it == spi.segIdx }) {
                        return false
                    }
                    for (b in buffer) {
                        for (spi2 in connections[b].spis) {
                            if (spi.segIdx == spi2.segIdx && spi.pointIdx != spi2.pointIdx) {
                                return true
                            }
                        }
                    }
                    return false
                }

                when (buffer.size) {
                    0 -> {
                        for (spi in con.spis)
                            onSuccess(spi, connIdx, false)
                    }
                    3 -> {
                        // we check both segments for the last one
                        // (we may have something G shaped)
                        // x---x
                        // |
                        // |  -x
                        // |   |
                        // x---x
                        if (verify(con.a) && verify(con.b)) {
                            buffer.add(ConnectionIdx(connIdx))
                            arrayOfRes.add(segmentBuffersToPoints(buffer)) // ArrayList(buffer))

                            buffer.removeAt(3)
                        }
                    }
                    else -> {
                        for (spi in con.spis) {
                            if (verify(spi)) {
                                onSuccess(spi, connIdx, true)
                                //break
                            }
                        }
                    }
                }
            }
        }
        override fun update(args: IntArray, isFastForward: Boolean) {
            super.update(args, isFastForward)
//            val ratio = args[2] * 0.1

//            remainingIters = 10
//            generateQuads(ratio)
            val res = connectionQuads
            if (res.isEmpty()) {
                // No board found.
                // We'll solve this later - in EditSquareFragment
                // (we'll redo some previous steps)
                cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
            } else {
                val idx = min(args[0], res.size - 1)
                val angleCloseEnough = args[1].toRad()

                resultQuad = res[idx].quad

                resultQuad!!.sortCW(angleCloseEnough)
                cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
                resultQuad!!.drawTo(previewMat, Colors.green, 5)
            }
        }



        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
                update(p)

                frag.setImagePreview(previewMat)// setImageGrayscalePreview(resultMat)
            }
        }
    }
}

