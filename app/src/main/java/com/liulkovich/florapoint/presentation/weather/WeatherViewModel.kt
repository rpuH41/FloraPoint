package com.liulkovich.florapoint.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.data.weather.WeatherService
import com.liulkovich.florapoint.presentation.screens.map.CurrentWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherService: WeatherService
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _currentWeather = MutableStateFlow<CurrentWeather?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    fun updateLocation(lat: Double, lon: Double) {
        _currentLocation.value = lat to lon
        loadWeather(lat, lon)
    }

    private fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val weather = weatherService.getWeatherData(lat, lon)
            if (weather?.temperature != null &&
                weather.humidity != null &&
                weather.avgTemp5Days != null &&
                weather.avgHumidity5Days != null
            ) {
                _currentWeather.value = CurrentWeather(
                    temperature = weather.temperature,
                    humidity = weather.humidity,
                    avgTemp5Days = weather.avgTemp5Days,
                    avgHumidity5Days = weather.avgHumidity5Days
                )
            }
        }
    }
}