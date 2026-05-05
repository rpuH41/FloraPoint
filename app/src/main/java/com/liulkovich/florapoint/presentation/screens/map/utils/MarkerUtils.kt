package com.liulkovich.florapoint.presentation.screens.map.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

fun createShapeMarkerBitmap(category: String): Bitmap {
    val width = 70
    val height = 90
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val cx = width / 2f

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val lower = category.lowercase().trim()

    val mainColor = when (lower) {
        "mushroom" -> "#795548".toColorInt()
        "berry"    -> "#E91E63".toColorInt()
        "plant"    -> "#2E7D32".toColorInt()
        "nut"      -> "#6D4C41".toColorInt()
        else       -> "#FF9800".toColorInt()
    }

    when (lower) {
        "mushroom" -> {
            fill.color = mainColor
            stroke.color = android.graphics.Color.WHITE
            stroke.strokeWidth = 4f

            val cap = Path().apply {
                moveTo(cx - 34f, 48f)
                quadTo(cx, 8f, cx + 34f, 48f)
                lineTo(cx - 34f, 48f)
                close()
            }
            canvas.drawPath(cap, fill)
            canvas.drawPath(cap, stroke)

            fill.color = "#D2B48C".toColorInt()
            val stem = RectF(cx - 9f, 48f, cx + 9f, 78f)
            canvas.drawRect(stem, fill)
            canvas.drawRect(stem, stroke)

            val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                alpha = 160
            }
            canvas.drawCircle(cx - 14f, 26f, 8f, highlight)
        }

        "berry" -> {
            fill.color = mainColor

            val radius = 26f
            val berryRect = RectF(cx - radius, 68f - radius * 0.9f, cx + radius, 68f + radius * 0.7f)
            canvas.drawOval(berryRect, fill)
            canvas.drawOval(berryRect, stroke)

            val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                alpha = 180
            }
            canvas.drawCircle(cx - 8f, 60f, 7f, highlightPaint)

            val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#2E7D32".toColorInt()
                style = Paint.Style.FILL
            }
            val leafPath = Path().apply {
                moveTo(cx, 48f)
                cubicTo(cx - 5f, 45f, cx - 10f, 48f, cx - 12f, 52f)
                cubicTo(cx - 8f, 50f, cx - 4f, 49f, cx, 48f)
                moveTo(cx, 48f)
                cubicTo(cx + 5f, 45f, cx + 10f, 48f, cx + 12f, 52f)
                cubicTo(cx + 8f, 50f, cx + 4f, 49f, cx, 48f)
            }
            canvas.drawPath(leafPath, leafPaint)

            val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#4A148C".toColorInt()
            }
            canvas.drawCircle(cx, 68f, 5f, centerPaint)
        }

        "plant" -> {
            fill.color = mainColor

            val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = mainColor
                strokeWidth = 8f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(cx, 48f, cx, 78f, stemPaint)

            val leftLeaf = Path().apply {
                moveTo(cx, 48f)
                cubicTo(cx - 12f, 48f, cx - 22f, 38f, cx - 22f, 30f)
                cubicTo(cx - 22f, 22f, cx - 12f, 22f, cx, 28f)
            }
            canvas.drawPath(leftLeaf, fill)
            canvas.drawPath(leftLeaf, stroke)

            val rightLeaf = Path().apply {
                moveTo(cx, 48f)
                cubicTo(cx + 12f, 48f, cx + 22f, 38f, cx + 22f, 30f)
                cubicTo(cx + 22f, 22f, cx + 12f, 22f, cx, 28f)
            }
            canvas.drawPath(rightLeaf, fill)
            canvas.drawPath(rightLeaf, stroke)
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
            canvas.drawCircle(cx, 48f, 26f, fill)
            stroke.color = android.graphics.Color.WHITE
            stroke.strokeWidth = 5f
            canvas.drawCircle(cx, 48f, 26f, stroke)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 32f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("★", cx, 57f, textPaint)
        }
    }

    val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mainColor }
    val tail = Path().apply {
        moveTo(cx - 9f, 78f)
        lineTo(cx, 88f)
        lineTo(cx + 9f, 78f)
    }
    canvas.drawPath(tail, tailPaint)

    return bitmap
}