package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity("user_points_table",
        foreignKeys = [
            ForeignKey(
                entity = Reference::class,
                parentColumns = ["id"],
                childColumns = ["species_id"],
                onDelete = ForeignKey.NO_ACTION,
                onUpdate = ForeignKey.NO_ACTION
            )
])
data class UserPoints(

    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
 @ColumnInfo(name = "species_id") val speciesId: Int,
 @ColumnInfo(name = "latitude") val latitude: Double,
 @ColumnInfo(name = "longitude") val longitude: Double,
 @ColumnInfo(name = "user_note") val userNote: String,
 @ColumnInfo(name = "timestamp") val timestamp: Int,
 @ColumnInfo(name = "is_favorite") val isFavorite: Int,
 @ColumnInfo(name = "photo_path") val photoPath: String,
 @ColumnInfo(name = "accuracy") val accuracy: Int
)
