package com.azbyn.chess_solver.step1

import com.azbyn.chess_solver.*
import com.azbyn.chess_solver.step1.PointQuad
import com.azbyn.chess_solver.step1.ConnectSegmentsFragment.VM.Connection
import com.azbyn.chess_solver.step1.ConnectSegmentsFragment.VM.SegmentPointIndex
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.COLOR_GRAY2RGB
import org.opencv.imgproc.Imgproc.cvtColor
import kotlin.math.min

class FindBoardFragment : BaseSlidersFragment(
    //SliderData("radius", default=50, min = 5, max = 200, stepSize =5),
    //SliderData("unused", default=20, min=5, max=90, stepSize=5)
//    SliderData("spacing", default=10, min = 5, max = 100, stepSize =5)
    SliderData("i", default=0, min = 0, max = 500, stepSize =1),
    SliderData("angle", default = 15, min = 1, max = 45, stepSize = 1)
) {
    override val viewModel: VM by viewModelDelegate()
    override val topBarName: String get() = "Find board"

    class VM : SlidersViewModel() {
        private val inViewModel: ConnectSegmentsFragment.VM by viewModelDelegate()
        private val segments get() = getViewModel<ProbabilisticLineMergeFragment.VM>().segments
        private val connections get() = inViewModel.connections

        private val ogMat get() = getViewModel<AcceptFragment.VM>().resultMat
        private var previewMat = Mat()

        var resultQuad: PointQuad? = null
            private set

        private var remainingIters = 0

        override fun init(frag: BaseFragment) {
            super.init(frag)
            remainingIters = 10
            generateQuads()
        }
        class ConnectionQuad(val quad: PointQuad)

        private val /*allQuads*/ connectionQuads = arrayListOf<ConnectionQuad>()
        private fun generateQuads() {
            connectionQuads.clear()
            impl(connectionQuads)
            connectionQuads.sortByDescending { it.quad.area() }

            //todo take into account how close the angles are to 90 deg

            logd("areas: ${connectionQuads.map { it.quad.area() }}")

            if (connectionQuads.isEmpty()) {
                redo(-10)
            } else {
                val dimensions = connectionQuads[0].quad.dimensions()
                logd("dim: $dimensions")
                //if it's too elongated, it's probably wrong
                if (dimensions.width *4 < dimensions.height ||
                    dimensions.height *4 < dimensions.width) {
//                    logd("wrong!")
                    if (remainingIters <= 0) return
                    --remainingIters
                    logd("iter-elongated $remainingIters")

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
//        fun redo(threshIncrease: Int, isFastForward: Boolean=true) {
            inViewModel.redo(threshIncrease, isFastForward=true, checkIters=false)
            generateQuads()
            //update(lastValues, isFastForward)

            return false
        }

        private fun segmentBuffersToPoints(ci: ArrayList<ConnectionIdx>): ConnectionQuad { // Array<Point> {
            return ConnectionQuad(
                PointQuad { ci[it].connection(connections).intersection })
//            return Array(4) { seg[it].second.getIntersection(segments) }
        }

        data class ConnectionIdx(/*val spi: SegmentPointIndex, */val connIdx: Int) {
            fun connection(connections: ArrayList<Connection>) = connections[connIdx]
        }

        private fun impl(arrayOfRes: ArrayList<ConnectionQuad>,
                         level: Int = 0,
                         startConnectionIdx: Int = 0,
                         completedSegmentsIndices: ArrayList<Int> = arrayListOf(),
                         buffer: ArrayList<ConnectionIdx> = arrayListOf()
        ) {
            fun log(@Suppress("UNUSED_PARAMETER") s: String) {
//                Log.d("azbyn-chess", "  ".repeat(level)+"|$s")
            }
            fun onSuccess(spi: SegmentPointIndex, connIdx: Int, addToCompletedSegments: Boolean) {
                val lastBufferIdx = buffer.size
                val lastCompletedSegmentIdx = completedSegmentsIndices.size
                buffer.add(ConnectionIdx(/*spi, */connIdx))
                if (addToCompletedSegments) {
                    completedSegmentsIndices.add(spi.segIdx)
                }
                log("succ1: ${buffer.map { it.connIdx }}")
                log("succ2: ${buffer.map { connections[it.connIdx] }}")
                log("succ3: $completedSegmentsIndices")


                if (buffer.size == 4) {
                    logd("yay: ${buffer.map { it.connIdx }.toStr()}")
//                    val buf = // buffer.clone().

                    val new = segmentBuffersToPoints(buffer)
                    if (!arrayOfRes.contains(new))
                        arrayOfRes.add(new) // ArrayList(buffer))
                } else {
                    impl(arrayOfRes, level+1,
                        connIdx+1, completedSegmentsIndices, buffer)
                }
                buffer.removeAt(lastBufferIdx)
                if (addToCompletedSegments) {
                    completedSegmentsIndices.removeAt(lastCompletedSegmentIdx)
                }
            }
//            logi("->  impl($startConnectionIdx): ${buffer.map { it.connIdx }}")
            log("impl(@$startConnectionIdx): ${buffer.map { it.connIdx }}")

            for (connIdx in startConnectionIdx until connections.size) {
                log("coni: $connIdx")
                val con = connections[connIdx]

                fun verify(spi: SegmentPointIndex): Boolean {
//                    if (buffer.isEmpty()) {
//                        onSuccess(spi, connIdx, false)
//                        return
//                    }
                    if (completedSegmentsIndices.any { it == spi.segIdx }) {
                        log("already there - ${spi.segIdx}")
                        return false
                    }
                    log("checking $connIdx -> $spi")
                    for (b in buffer) {
                        for (spi2 in b.connection(connections).spis) {
                            //log("checking $spi vs $spi2")
                            if (spi.segIdx == spi2.segIdx && spi.pointIdx != spi2.pointIdx) {

//                                onSuccess(spi, connIdx, true)
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
                            buffer.add(ConnectionIdx(/*spi, */connIdx))
                            logd("yay: ${buffer.map { it.connIdx }.toStr()}")
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

            /*val res2 = arrayListOf<PointQuad>()// Array<Point>>()
            impl(res2)

            val res = res2.distinct().sortedBy { it.area() }
*/

            val res = connectionQuads
            if (res.isEmpty()) {
                //if (redo(-10)) {
                //we'll solve this later - in EditSquareFragment
                cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
                //}
            } else {
                logi("result: ${res.toStr()}")

                val idx = min(args[0], res.size - 1)
                val angleCloseEnough = args[1].toRad()

                resultQuad = res[idx].quad
                //res.sortBy { it.area() }

//            for ((i, a) in res.withIndex()) {
//                a.sortCW(angleCloseEnough)
//                val col = if (i == idx) Colors.green else Colors.red
//                a.drawTo(previewMat, col, 5)
//            }
                resultQuad!!.sortCW(angleCloseEnough)
                cvtColor(ogMat, previewMat, COLOR_GRAY2RGB)
                resultQuad!!.drawTo(previewMat, Colors.green, 5)
                logi("$idx/${res.size}: ${resultQuad!!.points.toStr()}")
            }
        }



        override fun update(frag: ImageViewFragment, p: IntArray) {
            frag.tryOrComplain {
//                logTimeSec { update(p) }
                update(p)

                frag.setImagePreview(previewMat)// setImageGrayscalePreview(resultMat)
            }
        }
    }
}

