package com.liulkovich.florapoint.data.export

import android.content.Context
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

@Serializable
data class ExportPoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val userName: String,
    val description: String,
    val category: String,
    val timestamp: Long
)

class JsonExporter @Inject constructor() {

    private val json = Json { prettyPrint = true }

    fun export(context: Context, points: List<UserPoints>): File {
        val exportPoints = points.map { point ->
            ExportPoint(
                id = point.id,
                latitude = point.latitude,
                longitude = point.longitude,
                userName = point.userName,
                description = point.description,
                category = point.category ?: "custom",
                timestamp = point.timestamp
            )
        }

        val file = File(context.cacheDir, "florapoint_export.json")
        file.writeText(json.encodeToString(exportPoints))
        return file
    }
}