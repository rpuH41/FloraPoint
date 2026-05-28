package com.liulkovich.florapoint.data

import android.content.Context
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.OfflineRegion
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadProgress(
    val current: Int = 0,
    val total: Int = 0,
    val isFinished: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

@Singleton
class TileDownloadManager @Inject constructor(
    private val repository: FloraRepository
) {

    companion object {
        const val MAX_TILES = 35000
        const val MIN_ZOOM = 12
        const val MAX_ZOOM = 16
        const val BYTES_PER_TILE = 15_000L
    }

    fun countTiles(boundingBox: BoundingBox): Int {
        var count = 0
        for (zoom in MIN_ZOOM..MAX_ZOOM) {
            val x1 = lon2tile(boundingBox.lonWest, zoom)
            val x2 = lon2tile(boundingBox.lonEast, zoom)
            val y1 = lat2tile(boundingBox.latNorth, zoom)
            val y2 = lat2tile(boundingBox.latSouth, zoom)
            count += (kotlin.math.abs(x2 - x1) + 1) * (kotlin.math.abs(y2 - y1) + 1)
        }
        return count
    }

    suspend fun downloadTiles(
        context: Context,
        boundingBox: BoundingBox,
        regionName: String,
        mapView: MapView,
        onProgress: (DownloadProgress) -> Unit
    ) {
        val total = countTiles(boundingBox)

        if (total > MAX_TILES) {
            onProgress(
                DownloadProgress(
                    isError = true,
                    errorMessage = context.getString(
                        R.string.area_too_large_error,
                        total,
                        MAX_TILES
                    )
                )
            )
            return
        }

        val regionsCount = repository.countOfflineRegions()
        if (regionsCount >= 5) {
            onProgress(
                DownloadProgress(
                    isError = true,
                    errorMessage = context.getString(R.string.region_limit_reached)
                )
            )
            return
        }

        var downloaded = 0

        try {
            for (zoom in MIN_ZOOM..MAX_ZOOM) {
                val x1 = lon2tile(boundingBox.lonWest, zoom)
                val x2 = lon2tile(boundingBox.lonEast, zoom)
                val y1 = lat2tile(boundingBox.latNorth, zoom)
                val y2 = lat2tile(boundingBox.latSouth, zoom)

                for (x in kotlin.math.min(x1, x2)..kotlin.math.max(x1, x2)) {
                    for (y in kotlin.math.min(y1, y2)..kotlin.math.max(y1, y2)) {
                        try {
                            val tile = org.osmdroid.util.MapTileIndex.getTileIndex(zoom, x, y)
                            mapView.tileProvider.getMapTile(tile)

                            downloaded++
                            onProgress(DownloadProgress(current = downloaded, total = total))

                            if (downloaded % 150 == 0) {
                                Thread.sleep(60)
                            }
                        } catch (e: Exception) {

                        }
                    }
                }
            }

            val region = OfflineRegion(
                id = UUID.randomUUID().toString(),
                name = regionName,
                latNorth = boundingBox.latNorth,
                latSouth = boundingBox.latSouth,
                lonWest = boundingBox.lonWest,
                lonEast = boundingBox.lonEast,
                minZoom = MIN_ZOOM,
                maxZoom = MAX_ZOOM,
                tilesCount = downloaded,
                sizeBytes = downloaded * BYTES_PER_TILE,
                createdAt = System.currentTimeMillis()
            )
            repository.saveOfflineRegion(region)

            onProgress(DownloadProgress(current = downloaded, total = total, isFinished = true))

        } catch (e: Exception) {
            onProgress(DownloadProgress(isError = true, errorMessage = context.getString(
                R.string.error_format,
                e.message
            )))
        }
    }

    private fun lon2tile(lon: Double, zoom: Int): Int =
        ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

    private fun lat2tile(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        return ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad))
                / Math.PI) / 2.0 * (1 shl zoom)).toInt()
    }
}