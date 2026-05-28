package com.liulkovich.florapoint.data

data class CloudPointDto(
    val cloudId: String = "",
    val ownerUid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userName: String = "",
    val description: String = "",
    val category: String = "",
    val speciesId: Int? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)