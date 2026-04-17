package com.liulkovich.florapoint.presentation.screens.map.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap

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
        strokeWidth = 3.5f
    }

    when (category.lowercase()) {

        "mushroom" -> {
            fill.color = android.graphics.Color.BLACK
            stroke.color = android.graphics.Color.BLACK
            stroke.strokeWidth = 6f

            // Шляпка
            val cap = Path().apply {
                moveTo(cx - 29f, 42f)
                lineTo(cx + 29f, 42f)
                quadTo(cx, 16f, cx - 29f, 42f)
            }
            canvas.drawPath(cap, fill)
            canvas.drawPath(cap, stroke)

            // Ножка
            val stem = RectF(cx - 9f, 42f, cx + 9f, 78f)
            canvas.drawRect(stem, fill)
            canvas.drawRect(stem, stroke)
        }

        "berry" -> {  // Ягода (клубника/малина)
            fill.color = "#E91E63".toColorInt()
            val berry = Path().apply {
                moveTo(cx, 68f)
                cubicTo(cx - 22f, 48f, cx - 25f, 32f, cx, 22f)
                cubicTo(cx + 25f, 32f, cx + 22f, 48f, cx, 68f)
            }
            canvas.drawPath(berry, fill)
            canvas.drawPath(berry, stroke)

            // Семечки
            val seedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.Yellow.toArgb() }
            canvas.drawCircle(cx - 7f, 48f, 2.5f, seedPaint)
            canvas.drawCircle(cx + 6f, 45f, 2f, seedPaint)
            canvas.drawCircle(cx + 2f, 55f, 2.5f, seedPaint)
        }

        "plant" -> {  // Зелёное растение / трава
            fill.color = "#2E7D32".toColorInt()
            // Три листа
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

            // Стебель
            fill.color = "#1B5E20".toColorInt()
            canvas.drawRect(cx - 3f, 48f, cx + 3f, 78f, fill)
        }

        "nut" -> {  // Орех (фундук)
            fill.color = "#6D4C41".toColorInt()
            val nut = RectF(cx - 19f, 32f, cx + 19f, 68f)
            canvas.drawOval(nut, fill)
            canvas.drawOval(nut, stroke)

            // Шапочка
            fill.color = "#4E342E".toColorInt()
            val cap = RectF(cx - 21f, 22f, cx + 21f, 38f)
            canvas.drawOval(cap, fill)
            canvas.drawOval(cap, stroke)
        }

        else -> {  // Пользовательский или неизвестный
            fill.color = android.graphics.Color.GRAY
            canvas.drawCircle(cx, 48f, 24f, fill)
            canvas.drawCircle(cx, 48f, 24f, stroke)
        }
    }

    // Хвостик маркера (всегда один и тот же)
    val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (category == "mushroom") "#C62828".toColorInt()
        else fill.color
    }
    val tail = Path().apply {
        moveTo(cx - 9f, 78f)
        lineTo(cx, 88f)
        lineTo(cx + 9f, 78f)
    }
    canvas.drawPath(tail, tailPaint)

    return bitmap
}