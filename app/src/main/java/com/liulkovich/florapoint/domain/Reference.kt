package com.liulkovich.florapoint.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("reference_table")
data class Reference(
    @PrimaryKey
    @ColumnInfo(name = "id")val id: Int,
    @ColumnInfo(name = "category")val category: String,
    @ColumnInfo(name = "name")val name: String,
    @ColumnInfo(name = "habitat")val habitat: String,
    @ColumnInfo(name = "look_alikes")val lookAlikes: String,
    @ColumnInfo(name = "description")val description: String,
    @ColumnInfo(name = "start_month")val startMonth: Int,
    @ColumnInfo(name = "end_month")val endMonth: Int,
    @ColumnInfo(name = "image_name")val imageName: String,
    @ColumnInfo(name = "is_notif_enabled")val isNotifEnabled: Int

)
