package com.liulkovich.florapoint.domain.cloud

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class CloudPoint(
    @get:PropertyName("cloudId")
    val cloudId: String = "",

    @get:PropertyName("ownerUid")
    val ownerUid: String = "",

    @get:PropertyName("speciesId")
    val speciesId: Int? = null,

    @get:PropertyName("userName")
    val userName: String = "",

    @get:PropertyName("description")
    val description: String = "",

    @get:PropertyName("latitude")
    val latitude: Double = 0.0,

    @get:PropertyName("longitude")
    val longitude: Double = 0.0,

    @get:PropertyName("category")
    val category: String = "custom",

    @get:PropertyName("timestamp")
    val timestamp: Long = 0,

    @get:PropertyName("temperature")
    val temperature: Double? = null,

    @get:PropertyName("humidity")
    val humidity: Int? = null,

    @get:PropertyName("avgTemp5Days")
    val avgTemp5Days: Double? = null,

    @get:PropertyName("avgHumidity5Days")
    val avgHumidity5Days: Int? = null,

    @get:PropertyName("photoUrl")
    val photoUrl: String? = null
)