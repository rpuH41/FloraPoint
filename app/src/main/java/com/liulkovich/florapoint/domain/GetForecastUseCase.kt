package com.liulkovich.florapoint.domain

import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GetForecastUseCase @Inject constructor(
    private val repository: FloraRepository
) {

    suspend operator fun invoke(
        temperature: Double,
        humidity: Int,
        avgTemp: Double,
        avgHumidity: Int,
        rainSum5Days: Double,
        currentMonth: Int,
        currentLat: Double,
        currentLon: Double,
        userPoints: List<UserPoints>
    ): List<ForecastItem> {

        val allSpecies = repository.getAllSpeciesList()
        val allConditions = repository.getAllConditions()

        return allSpecies.mapNotNull { species ->
            val conditions = allConditions.find { it.speciesId == species.id }
                ?: return@mapNotNull null

            val score = calculateScore(
                avgTemp = avgTemp,
                avgHumidity = avgHumidity,
                rainSum5Days = rainSum5Days,
                currentMonth = currentMonth,
                species = species,
                conditions = conditions
            )

            if (score < 25) return@mapNotNull null

            val nearbyPoint = userPoints
                .filter { it.speciesId == species.id }
                .minByOrNull { point ->
                    distanceKm(currentLat, currentLon, point.latitude, point.longitude)
                }

            val distanceKm = nearbyPoint?.let {
                distanceKm(currentLat, currentLon, it.latitude, it.longitude)
            } ?: 999.0

            ForecastItem(
                reference = species,
                score = score,
                hasNearbyPoint = nearbyPoint != null,
                nearbyPointId = nearbyPoint?.id,
                nearbyLat = nearbyPoint?.latitude,
                nearbyLon = nearbyPoint?.longitude,
                nearbyDistanceKm = distanceKm.takeIf { nearbyPoint != null }
            )
        }
            .sortedByDescending { it.score }
    }

    private fun calculateScore(
        avgTemp: Double,
        avgHumidity: Int,
        rainSum5Days: Double,
        currentMonth: Int,
        species: Reference,
        conditions: SpeciesConditions
    ): Int {
        val inSeason = if (species.startMonth <= species.endMonth) {
            currentMonth in species.startMonth..species.endMonth
        } else {
            currentMonth >= species.startMonth || currentMonth <= species.endMonth
        }
        if (!inSeason) return 0

        val tempScore = when {
            avgTemp < conditions.tempMin - 4 || avgTemp > conditions.tempMax + 4 -> 0
            avgTemp in (conditions.tempMin.toDouble())..(conditions.tempMax.toDouble()) -> 35
            else -> 15
        }

        val humidityScore = when {
            avgHumidity < conditions.humidityMin - 12 || avgHumidity > conditions.humidityMax + 12 -> 0
            avgHumidity in conditions.humidityMin..conditions.humidityMax -> 25
            else -> 12
        }

        var score = 45 + tempScore + humidityScore

        if (tempScore == 0 || humidityScore == 0) {
            score = score.coerceAtMost(79)
        }

        if (rainSum5Days <= 0.0) {
            score = score.coerceAtMost(79)
        }

        return score.coerceIn(0, 100)
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}