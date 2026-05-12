package com.liulkovich.florapoint.domain

import android.content.Context
import android.net.Uri
import com.liulkovich.florapoint.data.export.GpxExporter
import com.liulkovich.florapoint.data.export.GpxImporter
import com.liulkovich.florapoint.data.export.JsonExporter
import com.liulkovich.florapoint.data.export.JsonImporter
import java.io.File
import javax.inject.Inject

class ExportPointsUseCase @Inject constructor(
    private val gpxExporter: GpxExporter,
    private val jsonExporter: JsonExporter,
    private val repository: FloraRepository
) {
    suspend operator fun invoke(context: Context, format: ExportFormat): File {
        val points = repository.getAllUserPointsList()
        return when (format) {
            ExportFormat.GPX -> gpxExporter.export(context, points)
            ExportFormat.JSON -> jsonExporter.export(context, points)
        }
    }
}

class ImportPointsUseCase @Inject constructor(
    private val gpxImporter: GpxImporter,
    private val jsonImporter: JsonImporter,
    private val repository: FloraRepository
) {
    suspend operator fun invoke(context: Context, uri: Uri, format: ExportFormat) {
        val points = when (format) {
            ExportFormat.GPX -> gpxImporter.import(context, uri)
            ExportFormat.JSON -> jsonImporter.import(context, uri)
        }
        points.forEach { repository.addNewPoint(it) }
    }
}

enum class ExportFormat { GPX, JSON }