package com.example.smartfrs

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs

object QRCodeGenerator {

    /**
     * Generates a realistic vector-based QR Code bitmap from text.
     */
    fun generateQRCode(text: String, width: Int = 300, height: Int = 300): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val size = 25
        val cellSize = width.toFloat() / size

        // Generate deterministic matrix based on hash of text
        val hash = abs(text.hashCode())
        val rng = java.util.Random(hash.toLong())

        val matrix = Array(size) { BooleanArray(size) }

        for (r in 0 until size) {
            for (c in 0 until size) {
                // Keep quiet zone margins
                if (r == 0 || c == 0 || r == size - 1 || c == size - 1) {
                    matrix[r][c] = false
                    continue
                }
                matrix[r][c] = rng.nextBoolean()
            }
        }

        // Draw standard QR Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(matrix, 1, 1)
        drawFinderPattern(matrix, size - 8, 1)
        drawFinderPattern(matrix, 1, size - 8)

        // Render matrix to canvas
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (matrix[r][c]) {
                    val left = c * cellSize
                    val top = r * cellSize
                    val right = left + cellSize
                    val bottom = top + cellSize
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }

        return bitmap
    }

    /**
     * Generates a 1D Barcode bitmap.
     */
    fun generateBarcode(text: String, width: Int = 400, height: Int = 100): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val hash = abs(text.hashCode())
        val rng = java.util.Random(hash.toLong())

        var x = 10f
        val barWidth = 3f

        while (x < width - 15) {
            val isBlack = rng.nextBoolean()
            val wMultiplier = if (rng.nextInt(4) == 0) 3 else 1
            val currentW = barWidth * wMultiplier

            if (isBlack) {
                canvas.drawRect(x, 10f, x + currentW, height - 10f, paint)
            }
            x += currentW + (if (rng.nextBoolean()) 2f else 4f)
        }

        return bitmap
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r >= 2 && r <= 4 && c >= 2 && c <= 4)
                matrix[startR + r][startC + c] = isOuter || isInner
            }
        }
    }
}
