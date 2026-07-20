package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("reference_table")
data class Reference(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "name_ru") val nameRu: String,
    @ColumnInfo(name = "name_en") val nameEn: String?,
    @ColumnInfo(name = "habitat_ru") val habitatRu: String,
    @ColumnInfo(name = "habitat_en") val habitatEn: String?,
    @ColumnInfo(name = "look_alikes_ru") val lookAlikesRu: String,
    @ColumnInfo(name = "look_alikes_en") val lookAlikesEn: String?,
    @ColumnInfo(name = "description_ru") val descriptionRu: String,
    @ColumnInfo(name = "description_en") val descriptionEn: String?,
    @ColumnInfo(name = "start_month") val startMonth: Int,
    @ColumnInfo(name = "end_month") val endMonth: Int,
    @ColumnInfo(name = "image_name") val imageName: String,
    @ColumnInfo(name = "is_notif_enabled") val isNotifEnabled: Int,
    @ColumnInfo(name = "is_reference_only") val isReferenceOnly: Int = 0,
    @ColumnInfo(name = "differences_ru") val differencesRu: String? = null,
    @ColumnInfo(name = "differences_en") val differencesEn: String? = null,
)