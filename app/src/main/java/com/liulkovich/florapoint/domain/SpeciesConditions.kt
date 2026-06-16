package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "species_conditions",
)
data class SpeciesConditions(
    @PrimaryKey
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "temp_min") val tempMin: Int,
    @ColumnInfo(name = "temp_max") val tempMax: Int,
    @ColumnInfo(name = "humidity_min") val humidityMin: Int,
    @ColumnInfo(name = "humidity_max") val humidityMax: Int
)
