package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_points_table",
    foreignKeys = [
        ForeignKey(
            entity = Reference::class,
            parentColumns = ["id"],
            childColumns = ["species_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("species_id")]
)
data class UserPoints(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,

    @ColumnInfo(name = "species_id")
    val speciesId: Int? = null,

    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int,
    @ColumnInfo(name = "photo_path") val photoPath: String,
    @ColumnInfo(name = "accuracy") val accuracy: Int,
    @ColumnInfo(name = "category") val category: String? = "custom",

    @ColumnInfo(name = "temperature") val temperature: Double? = null,
    @ColumnInfo(name = "humidity") val humidity: Int? = null,
    @ColumnInfo(name = "avg_temp_5days") val avgTemp5Days: Double? = null,
    @ColumnInfo(name = "avg_humidity_5days") val avgHumidity5Days: Int? = null,
    @ColumnInfo(name = "weather_timestamp") val weatherTimestamp: Long? = null,

    @ColumnInfo(name = "is_public") val isPublic: Boolean = false,
    @ColumnInfo(name = "owner_uid") val ownerUid: String? = null,
    @ColumnInfo(name = "cloud_id") val cloudId: String? = null,
    @ColumnInfo(name = "sync_state") val syncState: String = SyncState.LOCAL.name

)