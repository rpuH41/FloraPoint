package com.liulkovich.florapoint.data.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherData(
    val temperature: Double?,
    val humidity: Int?,
    val avgTemp5Days: Double?,
    val avgHumidity5Days: Int?
)

@Singleton
class WeatherService @Inject constructor() {

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherData? {
        return try {
            withContext(Dispatchers.IO) {
                val url = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m" +
                        "&daily=temperature_2m_mean,relative_humidity_2m_mean" +
                        "&past_days=5" +
                        "&timezone=auto"

                val response = URL(url).readText()
                val json = JSONObject(response)

                val current = json.getJSONObject("current")
                val daily = json.getJSONObject("daily")

                val tempArray = daily.getJSONArray("temperature_2m_mean")
                val humArray = daily.getJSONArray("relative_humidity_2m_mean")

                var sumTemp = 0.0
                var sumHum = 0

                for (i in 0 until tempArray.length()) {
                    sumTemp += tempArray.getDouble(i)
                    sumHum += humArray.getInt(i)
                }

                WeatherData(
                    temperature = current.getDouble("temperature_2m"),
                    humidity = current.getInt("relative_humidity_2m"),
                    avgTemp5Days = sumTemp / tempArray.length(),
                    avgHumidity5Days = sumHum / humArray.length()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
