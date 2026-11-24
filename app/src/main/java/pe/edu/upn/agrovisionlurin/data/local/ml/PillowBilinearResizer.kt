package pe.edu.upn.agrovisionlurin.data.local.ml

import android.graphics.Bitmap
import kotlin.math.abs

/**
 * Matches the training RGB resize: Pillow 11.3 BILINEAR, including its reduction
 * triangle support, normalized 22-bit coefficients and rounding after EACH axis.
 * Android's createScaledBitmap filtering is not equivalent when shrinking photos.
 * Algorithm reference: https://github.com/python-pillow/Pillow/blob/11.3.0/src/libImaging/Resample.c
 * Only a source row and the horizontally reduced image are allocated in addition
 * to the decoded bitmap; no second full-resolution pixel array is necessary.
 */
internal object PillowBilinearResizer {
    private const val BITS = 22
    private const val UNIT = 1 shl BITS
    private const val HALF = UNIT / 2
    private data class Kernel(val start: Int, val weights: IntArray)

    private fun kernels(source: Int, target: Int): Array<Kernel> {
        val ratio = source.toDouble() / target
        val radius = maxOf(1.0, ratio)
        return Array(target) { index ->
            val center = (index + 0.5) * ratio
            val first = maxOf(0, (center - radius + 0.5).toInt())
            val end = minOf(source, (center + radius + 0.5).toInt())
            val weights = DoubleArray(end - first) { offset ->
                maxOf(0.0, 1.0 - abs((offset + first - center + 0.5) / radius))
            }
            val total = weights.sum()
            Kernel(first, IntArray(weights.size) { (weights[it] / total * UNIT + 0.5).toInt() })
        }
    }

    fun resizePixels(bitmap: Bitmap, width: Int = 224, height: Int = 224): IntArray {
        require(width > 0 && height > 0)
        val horizontal = kernels(bitmap.width, width)
        val vertical = kernels(bitmap.height, height)
        val row = IntArray(bitmap.width)
        val reduced = IntArray(width * bitmap.height)
        for (y in 0 until bitmap.height) {
            bitmap.getPixels(row, 0, bitmap.width, 0, y, bitmap.width, 1)
            for (x in 0 until width) {
                val kernel = horizontal[x]
                var r = HALF; var g = HALF; var b = HALF
                for (offset in kernel.weights.indices) {
                    val pixel = row[kernel.start + offset]
                    val weight = kernel.weights[offset]
                    r += ((pixel shr 16) and 255) * weight
                    g += ((pixel shr 8) and 255) * weight
                    b += (pixel and 255) * weight
                }
                reduced[y * width + x] = (255 shl 24) or
                    ((r shr BITS).coerceIn(0, 255) shl 16) or
                    ((g shr BITS).coerceIn(0, 255) shl 8) or (b shr BITS).coerceIn(0, 255)
            }
        }
        return IntArray(width * height) { index ->
            val y = index / width; val x = index % width
            val kernel = vertical[y]
            var r = HALF; var g = HALF; var b = HALF
            for (offset in kernel.weights.indices) {
                val pixel = reduced[(kernel.start + offset) * width + x]
                val weight = kernel.weights[offset]
                r += ((pixel shr 16) and 255) * weight
                g += ((pixel shr 8) and 255) * weight
                b += (pixel and 255) * weight
            }
            (255 shl 24) or ((r shr BITS).coerceIn(0, 255) shl 16) or
                ((g shr BITS).coerceIn(0, 255) shl 8) or (b shr BITS).coerceIn(0, 255)
        }
    }
}
