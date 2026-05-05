package com.liulkovich.florapoint.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class Tip(
    @PrimaryKey
    val id: Int,
    val text: String
)