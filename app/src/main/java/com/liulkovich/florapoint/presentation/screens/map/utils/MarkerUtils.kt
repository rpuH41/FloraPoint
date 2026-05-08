package com.liulkovich.florapoint.presentation.screens.map.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

private val markerCache = mutableMapOf<String, Bitmap>()

fun createShapeMarkerBitmap(category: String): Bitmap {
    return markerCache.getOrPut(category.lowercase().trim()) {
        val width = 72
        val height = 94
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        val cx = width / 2f

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3.8f
        }

        when (category.lowercase().trim()) {

            "mushroom" -> {
                fill.color = "#A0522D".toColorInt()
                val cap = Path().apply {
                    moveTo(cx - 30f, 46f)
                    quadTo(cx - 19f, 19f, cx, 16f)
                    quadTo(cx + 19f, 19f, cx + 30f, 46f)
                    close()
                }
                canvas.drawPath(cap, fill)
                canvas.drawPath(cap, stroke)

                fill.color = "#E8C9A0".toColorInt()
                val stem = RectF(cx - 8.5f, 46f, cx + 8.5f, 78f)
                canvas.drawRect(stem, fill)
                canvas.drawRect(stem, stroke)

                fill.color = android.graphics.Color.WHITE
                canvas.drawCircle(cx - 11f, 33f, 5f, fill)
                canvas.drawCircle(cx + 9f, 37f, 4f, fill)
            }

            "berry" -> {
                fill.color = "#6B3E9E".toColorInt()
                canvas.drawCircle(cx - 9f, 57f, 13.5f, fill)
                canvas.drawCircle(cx + 11f, 54f, 12.5f, fill)
                canvas.drawCircle(cx + 1f, 67f, 11.8f, fill)

                fill.color = "#C15EFF".toColorInt()
                canvas.drawCircle(cx - 9f, 56f, 9f, fill)
                canvas.drawCircle(cx + 11f, 53f, 8.5f, fill)
                canvas.drawCircle(cx + 1f, 66f, 7.8f, fill)

                fill.color = "#228B22".toColorInt()
                val leaf = Path().apply {
                    moveTo(cx + 3f, 46f)
                    cubicTo(cx - 13f, 35f, cx - 18f, 39f, cx - 10f, 48f)
                }
                canvas.drawPath(leaf, fill)
                canvas.drawPath(leaf, stroke)
            }

            "plant" -> {
                val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = "#1F6B1F".toColorInt()
                    strokeWidth = 6.5f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(cx, 79f, cx, 44f, stemPaint)

                fill.color = "#34C05A".toColorInt()

                val leaf1 = Path().apply {
                    moveTo(cx - 2f, 52f)
                    cubicTo(cx - 19f, 37f, cx - 26f, 29f, cx - 10f, 31f)
                    cubicTo(cx - 8f, 44f, cx - 5f, 49f, cx - 2f, 52f)
                }
                canvas.drawPath(leaf1, fill)
                canvas.drawPath(leaf1, stroke)

                val leaf2 = Path().apply {
                    moveTo(cx + 2f, 50f)
                    cubicTo(cx + 19f, 36f, cx + 25f, 28f, cx + 11f, 30f)
                    cubicTo(cx + 9f, 43f, cx + 6f, 47f, cx + 2f, 50f)
                }
                canvas.drawPath(leaf2, fill)
                canvas.drawPath(leaf2, stroke)

                val leaf3 = Path().apply {
                    moveTo(cx, 43f)
                    cubicTo(cx - 9f, 33f, cx - 12f, 28f, cx - 3f, 29f)
                    cubicTo(cx + 3f, 35f, cx + 4f, 39f, cx, 43f)
                }
                canvas.drawPath(leaf3, fill)
                canvas.drawPath(leaf3, stroke)
            }

            "nut" -> {
                val shellColor = "#D2A679".toColorInt()
                val capColor = "#8B5A2B".toColorInt()
                val lineColor = "#6B4226".toColorInt()

                fill.color = shellColor
                stroke.color = android.graphics.Color.WHITE
                stroke.strokeWidth = 4f

                val bodyPath = Path().apply {
                    moveTo(cx, 78f)
                    cubicTo(cx + 24f, 68f, cx + 28f, 44f, cx + 16f, 30f)
                    cubicTo(cx + 6f, 20f, cx - 6f, 20f, cx - 16f, 30f)
                    cubicTo(cx - 28f, 44f, cx - 24f, 68f, cx, 78f)
                    close()
                }
                canvas.drawPath(bodyPath, fill)
                canvas.drawPath(bodyPath, stroke)

                val capPath = Path().apply {
                    moveTo(cx - 20f, 32f)
                    cubicTo(cx - 22f, 22f, cx - 10f, 18f, cx, 18f)
                    cubicTo(cx + 10f, 18f, cx + 22f, 22f, cx + 20f, 32f)
                    cubicTo(cx + 14f, 28f, cx - 14f, 28f, cx - 20f, 32f)
                    close()
                }
                canvas.drawPath(capPath, fill.apply { color = capColor })
                canvas.drawPath(capPath, stroke)

                val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = lineColor
                    strokeWidth = 2.5f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(cx, 40f, cx, 72f, ridgePaint)
                canvas.drawLine(cx - 10f, 48f, cx - 8f, 70f, ridgePaint)
                canvas.drawLine(cx + 10f, 48f, cx + 8f, 70f, ridgePaint)

                val crackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = lineColor
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(cx - 4f, 52f, cx + 4f, 62f, crackPaint)
                canvas.drawLine(cx + 2f, 56f, cx - 2f, 66f, crackPaint)

                val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    alpha = 150
                }
                canvas.drawCircle(cx - 8f, 48f, 6f, highlightPaint)
            }

            else -> {
                fill.color = "#FF9800".toColorInt()
                canvas.drawCircle(cx, 52f, 26f, fill)
                stroke.strokeWidth = 5f
                canvas.drawCircle(cx, 52f, 26f, stroke)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = 34f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("?", cx, 61f, textPaint)
            }
        }

        // Хвостик
        val tailColor = when (category.lowercase().trim()) {
            "mushroom" -> "#A0522D".toColorInt()
            "berry" -> "#6B3E9E".toColorInt()
            "plant" -> "#1F6B1F".toColorInt()
            "nut" -> "#A36A2E".toColorInt()
            else -> "#FF9800".toColorInt()
        }

        val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tailColor }
        val tail = Path().apply {
            moveTo(cx - 9f, 78f)
            lineTo(cx, 90f)
            lineTo(cx + 9f, 78f)
        }
        canvas.drawPath(tail, tailPaint)

        bitmap
    }
}