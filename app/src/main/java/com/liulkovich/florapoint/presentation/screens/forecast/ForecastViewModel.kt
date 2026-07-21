package com.liulkovich.florapoint.presentation.screens.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.ForecastItem
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.GetForecastUseCase
import com.liulkovich.florapoint.presentation.screens.map.CurrentWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getForecastUseCase: GetForecastUseCase,
    private val repository: FloraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForecastScreenState())
    val state = _state.asStateFlow()

    fun load(weather: CurrentWeather?, isWeatherLoading: Boolean, currentLat: Double, currentLon: Double) {
        if (weather == null && isWeatherLoading) {
            _state.update { it.copy(isLoading = true, noWeather = false) }
            return
        }

        if (weather == null) {
            _state.update { it.copy(isLoading = false, noWeather = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val userPoints = repository.getAllUserPointsList()

            val forecast = getForecastUseCase(
                temperature = weather.temperature,
                humidity = weather.humidity,
                avgTemp = weather.avgTemp5Days,
                avgHumidity = weather.avgHumidity5Days,
                rainSum5Days = weather.rainSum5Days,
                currentMonth = currentMonth,
                currentLat = currentLat,
                currentLon = currentLon,
                userPoints = userPoints
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    noWeather = false,
                    items = forecast,
                    temperature = weather.temperature,
                    humidity = weather.humidity,
                    avgTemp = weather.avgTemp5Days,
                    avgHumidity = weather.avgHumidity5Days,
                    currentMonth = currentMonth
                )
            }
        }
    }
}

data class ForecastScreenState(
    val isLoading: Boolean = true,
    val noWeather: Boolean = false,
    val items: List<ForecastItem> = emptyList(),
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val avgTemp: Double = 0.0,
    val avgHumidity: Int = 0,
    val currentMonth: Int = 0
)