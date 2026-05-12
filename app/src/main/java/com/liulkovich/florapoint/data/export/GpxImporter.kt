package com.liulkovich.florapoint.data.export

import android.content.Context
import android.net.Uri
import com.liulkovich.florapoint.domain.UserPoints
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class GpxImporter @Inject constructor() {

    fun import(context: Context, uri: Uri): List<UserPoints> {
        val points = mutableListOf<UserPoints>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var lat = 0.0
        var lon = 0.0
        var name = ""
        var desc = ""
        var time = 0L
        var category = "custom"
        var inWpt = false
        var inExtensions = false
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (currentTag) {
                        "wpt" -> {
                            inWpt = true
                            lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                            lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                            name = ""
                            desc = ""
                            time = System.currentTimeMillis() / 1000L
                            category = "custom"
                        }
                        "extensions" -> if (inWpt) inExtensions = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inWpt) {
                        when {
                            currentTag == "name" -> name = parser.text.trim()
                            currentTag == "desc" -> desc = parser.text.trim()
                            currentTag == "time" -> {
                                runCatching {
                                    time = dateFormat.parse(parser.text.trim())!!.time / 1000L
                                }
                            }
                            currentTag == "category" && inExtensions ->
                                category = parser.text.trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "wpt" -> {
                            if (inWpt && lat != 0.0 && lon != 0.0) {
                                points.add(
                                    UserPoints(
                                        id = 0, // Room сам присвоит id
                                        speciesId = null,
                                        latitude = lat,
                                        longitude = lon,
                                        userName = name,
                                        description = desc,
                                        category = category,
                                        timestamp = time,
                                        isFavorite = 0,
                                        photoPath = "",
                                        accuracy = 0
                                    )
                                )
                            }
                            inWpt = false
                        }
                        "extensions" -> inExtensions = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return points
    }
}