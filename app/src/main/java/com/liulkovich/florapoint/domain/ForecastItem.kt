package com.liulkovich.florapoint.domain

data class ForecastItem(
    val reference: Reference,
    val score: Int,
    val hasNearbyPoint: Boolean = false,
    val nearbyPointId: Int? = null,
    val nearbyLat: Double? = null,
    val nearbyLon: Double? = null
)