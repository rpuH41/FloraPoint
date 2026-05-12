package com.liulkovich.florapoint.data.export

import android.content.Context
import com.liulkovich.florapoint.domain.UserPoints
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GpxExporter @Inject constructor() {

    fun export(context: Context, points: List<UserPoints>): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val sb = StringBuilder()

        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="FloraPoint">""")

        points.forEach { point ->
            val time = dateFormat.format(Date(point.timestamp * 1000L))
            sb.appendLine("""  <wpt lat="${point.latitude}" lon="${point.longitude}">""")
            sb.appendLine("""    <name>${escapeXml(point.userName.ifBlank { "Point ${point.id}" })}</name>""")
            sb.appendLine("""    <desc>${escapeXml(point.description)}</desc>""")
            sb.appendLine("""    <time>$time</time>""")
            // Категория в расширении GPX
            sb.appendLine("""    <extensions>""")
            sb.appendLine("""      <category>${escapeXml(point.category ?: "custom")}</category>""")
            sb.appendLine("""    </extensions>""")
            sb.appendLine("""  </wpt>""")
        }

        sb.appendLine("""</gpx>""")

        val file = File(context.cacheDir, "florapoint_export.gpx")
        file.writeText(sb.toString())
        return file
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}