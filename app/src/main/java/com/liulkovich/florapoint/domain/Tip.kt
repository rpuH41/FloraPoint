package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class Tip(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "text_ru") val textRu: String,
    @ColumnInfo(name = "text_en") val textEn: String?
)