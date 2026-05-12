package com.liulkovich.florapoint.data.export

import android.content.Context
import android.net.Uri
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.serialization.json.Json
import javax.inject.Inject

class JsonImporter @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    fun import(context: Context, uri: Uri): List<UserPoints> {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val text = inputStream.bufferedReader().readText()
        inputStream.close()

        return runCatching {
            json.decodeFromString<List<ExportPoint>>(text).map { point ->
                UserPoints(
                    id = 0,
                    speciesId = null,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    userName = point.userName,
                    description = point.description,
                    category = point.category,
                    timestamp = point.timestamp,
                    isFavorite = 0,
                    photoPath = "",
                    accuracy = 0
                )
            }
        }.getOrElse { emptyList() }
    }
}