package com.liulkovich.florapoint.domain.cloud

import com.liulkovich.florapoint.domain.SyncState
import com.liulkovich.florapoint.domain.UserPoints

fun UserPoints.toCloudPoint(): CloudPoint {
    return CloudPoint(
        cloudId = cloudId ?: "",

        speciesId = speciesId,

        latitude = latitude,
        longitude = longitude,

        userName = userName,
        description = description,

        category = category ?: "custom",

        timestamp = timestamp,

        temperature = temperature,
        humidity = humidity,

        avgTemp5Days = avgTemp5Days,
        avgHumidity5Days = avgHumidity5Days,

        ownerUid = ownerUid ?: "",

        photoUrl = null
    )
}

fun CloudPoint.toUserPoint(): UserPoints {

    return UserPoints(
        id = 0,

        speciesId = speciesId,

        latitude = latitude,
        longitude = longitude,

        userName = userName,
        description = description,

        timestamp = timestamp,

        isFavorite = 0,
        photoPath = "",
        accuracy = 0,

        category = category,

        temperature = temperature,
        humidity = humidity,
        avgTemp5Days = avgTemp5Days,
        avgHumidity5Days = avgHumidity5Days,

        weatherTimestamp = null,

        isPublic = true,
        ownerUid = ownerUid,
        cloudId = cloudId,

        syncState = SyncState.SYNCED.name
    )
}