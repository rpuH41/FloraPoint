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
            val cap = Path().apply {
                moveTo(cx - 29f, 42f)
                lineTo(cx + 29f, 42f)
                quadTo(cx, 16f, cx - 29f, 42f)
            }
            canvas.drawPath(cap, fill)
            canvas.drawPath(cap, stroke)

            val stem = RectF(cx - 9f, 42f, cx + 9f, 78f)
            canvas.drawRect(stem, fill)
            canvas.drawRect(stem, stroke)
        }

        "berry" -> {
            fill.color = mainColor
            val berry = Path().apply {
                moveTo(cx, 68f)
                cubicTo(cx - 22f, 48f, cx - 25f, 32f, cx, 22f)
                cubicTo(cx + 25f, 32f, cx + 22f, 48f, cx, 68f)
            }
            canvas.drawPath(berry, fill)
            canvas.drawPath(berry, stroke)
        }

        "plant" -> {
            fill.color = mainColor
            val leaf1 = Path().apply {
                moveTo(cx - 6f, 75f)
                lineTo(cx - 22f, 35f)
                lineTo(cx - 5f, 25f)
                lineTo(cx + 8f, 42f)
                close()
            }
            val leaf2 = Path().apply {
                moveTo(cx + 6f, 75f)
                lineTo(cx + 22f, 35f)
                lineTo(cx + 5f, 25f)
                lineTo(cx - 8f, 42f)
                close()
            }
            canvas.drawPath(leaf1, fill)
            canvas.drawPath(leaf2, fill)
            canvas.drawPath(leaf1, stroke)
            canvas.drawPath(leaf2, stroke)

            fill.color = "#1B5E20".toColorInt()
            canvas.drawRect(cx - 3f, 48f, cx + 3f, 78f, fill)
        }

        "nut" -> {
            fill.color = mainColor
            val nut = RectF(cx - 19f, 32f, cx + 19f, 68f)
            canvas.drawOval(nut, fill)
            canvas.drawOval(nut, stroke)

            fill.color = "#4E342E".toColorInt()
            val cap = RectF(cx - 21f, 22f, cx + 21f, 38f)
            canvas.drawOval(cap, fill)
            canvas.drawOval(cap, stroke)
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