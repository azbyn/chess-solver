package com.azbyn.chess_solver.svm

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.opencv.core.CvType.CV_64F
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import kotlin.math.sqrt

fun Mat.toDoubleArray(): DoubleArray {
    if (this.type() == CV_64F) {
        return DoubleArray(width() * height()).apply {
            this@toDoubleArray.get(0, 0, this)
        }
    } else {
        val mat = Mat()
        this.convertTo(mat, CV_64F)
        return mat.toDoubleArray()
    }
}


object VectorSerializer : KSerializer<Vector> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Vector", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        Vector.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Vector) =
        encoder.encodeString(value.toString())
}

@Serializable(with = VectorSerializer::class)
class Vector(vararg elements: Double) {
    val values: DoubleArray = elements

    constructor(mat: Mat): this(*mat.toDoubleArray())

    val size get() = values.size

    override fun toString(): String {
        return values.joinToString(separator = ", ",prefix="{", postfix ="}") { it.toString() }
    }
    operator fun get(i: Int) = values[i]

    fun toMat(): Mat {
        return  MatOfFloat(*values.map { it.toFloat() }.toFloatArray()).reshape(1, 1)
    }

    companion object {
        fun fromString(s: String) = Vector(
            *s.removePrefix("{").removeSuffix("}")
                .splitToSequence(", ").map { it.toDouble() }
                .toList().toDoubleArray()
        )
    }
}

fun Vector.withIndex() = this.values.withIndex()

//typealias Vector = Mat

fun dot(x: Vector, y: Vector): Double {
    var res = 0.0
    for (i in 0 until x.size)
        res += x[i] * y[i]
    return res
}
fun dist2(x: Vector, y: Vector): Double {
    var res = 0.0
    for (i in 0 until x.size) {
        val e = (x[i] - y[i])
        res += e * e
    }
    return res
} //= norm(x.mat, y.mat, NORM_L2SQR)

fun dist(x: Vector, y: Vector): Double = sqrt(dist2(x, y))
//= norm(x.mat, y.mat, NORM_L2)

//operator fun Vector.get(i: Int): Double = mat.get(0, i)[0]
