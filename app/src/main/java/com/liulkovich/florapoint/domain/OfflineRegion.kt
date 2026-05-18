package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_regions")
data class OfflineRegion(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "lat_north") val latNorth: Double,
    @ColumnInfo(name = "lat_south") val latSouth: Double,
    @ColumnInfo(name = "lon_west") val lonWest: Double,
    @ColumnInfo(name = "lon_east") val lonEast: Double,
    @ColumnInfo(name = "min_zoom") val minZoom: Int = 12,
    @ColumnInfo(name = "max_zoom") val maxZoom: Int = 16,
    @ColumnInfo(name = "tiles_count") val tilesCount: Int,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long
)