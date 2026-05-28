package com.liulkovich.florapoint.domain.cloud

data class CloudPoint(
    val cloudId: String = "",
    val ownerUid: String = "",

    val speciesId: Int? = null,
    val userName: String = "",
    val description: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val category: String = "custom",

    val timestamp: Long = 0,

    val temperature: Double? = null,
    val humidity: Int? = null,
    val avgTemp5Days: Double? = null,
    val avgHumidity5Days: Int? = null,

    val photoUrl: String? = null
)