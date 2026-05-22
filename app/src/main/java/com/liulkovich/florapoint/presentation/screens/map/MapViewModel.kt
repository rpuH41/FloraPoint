package com.liulkovich.florapoint.presentation.screens.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.data.weather.WeatherService
import com.liulkovich.florapoint.domain.AddNewPointUseCase
import com.liulkovich.florapoint.domain.DeletePointUseCase
import com.liulkovich.florapoint.domain.EditPointUseCase
import com.liulkovich.florapoint.domain.FloraCategory
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetAllUserPointsUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAllUserPointsUseCase: GetAllUserPointsUseCase,
    private val deletePointUseCase: DeletePointUseCase,
    private val addNewPointUseCase: AddNewPointUseCase,
    private val editPointUseCase: EditPointUseCase,
    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    private val weatherService: WeatherService
) : ViewModel() {

    private val _state = MutableStateFlow(MapScreenState())
    val state = _state.asStateFlow()

    private val _command = MutableSharedFlow<MapCommand>(extraBufferCapacity = 1)
    val command = _command.asSharedFlow()

    init {
        getAllUserPointsUseCase()
            .onEach { userPoints ->
                _state.update { it.copy(userPoints = userPoints) }
            }
            .launchIn(viewModelScope)

        getAllSpeciesUseCase()
            .onEach { speciesList ->
                _state.update { it.copy(species = speciesList) }
            }
            .launchIn(viewModelScope)
    }

    fun selectPoint(pointId: Int?) {
        _state.update { it.copy(selectedPointId = pointId) }
    }

    fun updateCurrentLocation(lat: Double, lon: Double) {
        _state.update { it.copy(currentUserLocation = lat to lon) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onPointClicked(point: UserPoints) {
        selectPoint(point.id)
        viewModelScope.launch {
            _command.emit(MapCommand.CenterMapOnPoint(point))
        }
    }

    fun onAddNewPointClicked(lat: Double, lon: Double) {
        _state.update { it.copy(bottomSheetMode = BottomSheetMode.Add(lat, lon)) }
    }

    fun onPointLongClicked(point: UserPoints) {
        _state.update { it.copy(bottomSheetMode = BottomSheetMode.Edit(point.id)) }
    }

    fun dismissBottomSheet() {
        _state.update { it.copy(bottomSheetMode = null) }
    }
    fun updateMissingWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            val allPoints = state.value.userPoints

            val pointsToUpdate = allPoints.filter {
                it.avgTemp5Days == null || it.avgHumidity5Days == null
            }

            if (pointsToUpdate.isEmpty()) return@launch

            _command.emit(MapCommand.ShowMessage("Обновляем погоду..."))

            pointsToUpdate.forEach { point ->
                val weather = weatherService.getWeatherData(point.latitude, point.longitude)
                if (weather != null) {
                    val updated = point.copy(
                        temperature = weather.temperature,
                        humidity = weather.humidity,
                        avgTemp5Days = weather.avgTemp5Days,
                        avgHumidity5Days = weather.avgHumidity5Days,
                        weatherTimestamp = System.currentTimeMillis()
                    )
                    editPointUseCase(updated)
                }
            }
        }
    }

    fun addNewPoint(
        latitude: Double,
        longitude: Double,
        speciesId: Int?,
        userName: String,
        description: String,
        category: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val weather = weatherService.getWeatherData(latitude, longitude)

            val newPoint = UserPoints(
                speciesId = speciesId,
                latitude = latitude,
                longitude = longitude,
                userName = userName.ifBlank { "Неизвестный вид" },
                description = description.trim(),
                category = category,
                timestamp = System.currentTimeMillis() / 1000,

                temperature = weather?.temperature,
                humidity = weather?.humidity,
                avgTemp5Days = weather?.avgTemp5Days,
                avgHumidity5Days = weather?.avgHumidity5Days,
                weatherTimestamp = if (weather != null) System.currentTimeMillis() else null,

                isFavorite = 0,
                photoPath = "",
                accuracy = 0
            )

            addNewPointUseCase(newPoint)

            if (weather == null) {
                _command.emit(MapCommand.ShowMessage("Точка добавлена. Погода будет загружена при появлении интернета."))
            } else {
                _command.emit(MapCommand.ShowMessage("Точка добавлена с данными о погоде"))
            }
        }
    }

    fun updateUserPoint(
        pointId: Int,
        speciesId: Int?,
        userName: String,
        description: String,
        category: String
    ) {
        viewModelScope.launch {
            state.value.userPoints.find { it.id == pointId }?.let { old ->
                editPointUseCase(
                    old.copy(
                        speciesId = speciesId,
                        userName = userName,
                        description = description,
                        category = category
                    )
                )
            }
        }
    }

    fun deletePoint(pointId: Int) {
        viewModelScope.launch { deletePointUseCase(pointId) }
    }

    fun sharePoint(context: Context, point: UserPoints, speciesName: String) {
        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(point.timestamp * 1000L))

        val emoji = FloraCategory.fromKey(point.category ?: "")?.emoji ?: "📍"

        val isAppInstalled = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
            true
        } catch (e: Exception) {
            false
        }

        val appLink = if (isAppInstalled) {
            "florapoint://point?lat=${point.latitude}&lon=${point.longitude}&name=${Uri.encode(point.userName)}&category=${point.category ?: "custom"}"
        } else {
            "https://play.google.com/store/apps/details?id=${context.packageName}"
        }

        val text = buildString {
            appendLine("$emoji $speciesName")
            if (point.description.isNotBlank()) {
                appendLine("📝 ${point.description}")
            }
            appendLine("🗓 $dateStr")
            appendLine()
            appendLine("📍 https://maps.google.com/?q=${point.latitude},${point.longitude}")
            appendLine()
            append("🌿 Открыть в FloraPoint: $appLink")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться точкой"))
    }
    private var _deepLinkName = ""
    private var _deepLinkCategory = "custom"

    fun setDeepLinkData(name: String, category: String) {
        _deepLinkName = name
        _deepLinkCategory = category
        _state.update {
            it.copy(
                deepLinkName = name,
                deepLinkCategory = category
            )
        }
    }
}

sealed interface MapCommand {
    data class CenterMapOnPoint(val point: UserPoints) : MapCommand
    data class ShowDeleteConfirmation(val pointId: Int) : MapCommand
    data class ShowMessage(val text: String) : MapCommand
}

sealed interface BottomSheetMode {
    data class Add(val latitude: Double, val longitude: Double) : BottomSheetMode
    data class Edit(val pointId: Int) : BottomSheetMode
}

data class MapScreenState(
    val userPoints: List<UserPoints> = emptyList(),
    val selectedPointId: Int? = null,
    val currentUserLocation: Pair<Double, Double>? = null,
    val searchQuery: String = "",
    val species: List<Reference> = emptyList(),
    val bottomSheetMode: BottomSheetMode? = null,
    val deepLinkName: String = "",
    val deepLinkCategory: String = ""
)