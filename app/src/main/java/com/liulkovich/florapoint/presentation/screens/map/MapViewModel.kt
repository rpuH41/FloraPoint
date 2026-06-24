package com.liulkovich.florapoint.presentation.screens.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.data.weather.WeatherService
import com.liulkovich.florapoint.domain.AddNewPointUseCase
import com.liulkovich.florapoint.domain.DeletePointUseCase
import com.liulkovich.florapoint.domain.EditPointUseCase
import com.liulkovich.florapoint.domain.FloraCategory
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetAllUserPointsUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import com.liulkovich.florapoint.domain.cloud.FirestoreRepository
import com.liulkovich.florapoint.presentation.auth.AuthManager
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
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val getAllUserPointsUseCase: GetAllUserPointsUseCase,
    private val deletePointUseCase: DeletePointUseCase,
    private val addNewPointUseCase: AddNewPointUseCase,
    private val editPointUseCase: EditPointUseCase,
    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    private val weatherService: WeatherService,
    private val authManager: AuthManager,
    private val firestoreRepository: FirestoreRepository
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

        loadPublicPoints()
        updateMissingWeatherData()
    }

    private fun loadPublicPoints() {
        viewModelScope.launch {
            val publicPoints = firestoreRepository.downloadPublicPoints()

            _state.update {
                it.copy(publicPoints = publicPoints)
            }
        }
    }

    fun isAuthorized(): Boolean {
        return authManager.isAuthorized()
    }

    fun selectPoint(pointId: Int?) {
        _state.update { it.copy(selectedPointId = pointId) }
    }

    fun updateCurrentLocation(lat: Double, lon: Double) {
        _state.update { it.copy(currentUserLocation = lat to lon) }
        loadCurrentWeather(lat, lon)
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
        _state.update {
            it.copy(bottomSheetMode = BottomSheetMode.Add(lat, lon))
        }
    }

    fun onPointLongClicked(point: UserPoints) {
        _state.update {
            it.copy(bottomSheetMode = BottomSheetMode.Edit(point.id))
        }
    }

    fun dismissBottomSheet() {
        _state.update {
            it.copy(bottomSheetMode = null)
        }
    }

    fun updateMissingWeatherData() {
        //val updatingWeather = context.getString(R.string.updating_weather)
        viewModelScope.launch(Dispatchers.IO) {

            val allPoints = state.value.userPoints

            val pointsToUpdate = allPoints.filter {
                it.avgTemp5Days == null || it.avgHumidity5Days == null
            }

            if (pointsToUpdate.isEmpty()) return@launch

            _command.emit(
                MapCommand.ShowMessage(context.getString(R.string.updating_weather))
            )

            pointsToUpdate.forEach { point ->

                val weather =
                    weatherService.getWeatherData(
                        point.latitude,
                        point.longitude
                    )

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
        category: String,
        isPublic: Boolean
    ) {
        viewModelScope.launch {

            val tempPoint = UserPoints(
                speciesId = speciesId,
                latitude = latitude,
                longitude = longitude,
                userName = userName.ifBlank {
                    context.getString(R.string.unknown_species)
                },
                description = description.trim(),
                category = category,
                timestamp = System.currentTimeMillis() / 1000,
                temperature = null,
                humidity = null,
                avgTemp5Days = null,
                avgHumidity5Days = null,
                weatherTimestamp = null,
                isFavorite = 0,
                photoPath = "",
                accuracy = 0,
                isPublic = isPublic && authManager.isAuthorized(),
                ownerUid = authManager.getCurrentUserId()
            )

            _command.emit(
                MapCommand.ShowMessage(
                    context.getString(R.string.loadingl_eather)
                )
            )
            val newId = addNewPointUseCase(tempPoint)

            launch(Dispatchers.IO) {
                val weather = weatherService.getWeatherData(latitude, longitude)

                val updatedPoint = tempPoint.copy(
                    id = newId,
                    temperature = weather?.temperature,
                    humidity = weather?.humidity,
                    avgTemp5Days = weather?.avgTemp5Days,
                    avgHumidity5Days = weather?.avgHumidity5Days,
                    weatherTimestamp = if (weather != null) System.currentTimeMillis() else null
                )

                editPointUseCase(updatedPoint)

                if ( updatedPoint.isPublic && authManager.isAuthorized()) {
                    val result = firestoreRepository.uploadPoint(updatedPoint)
                    result.onSuccess { cloudId ->
                        editPointUseCase(
                            updatedPoint.copy(
                                cloudId = cloudId,
                                syncState = "SYNCED"
                            )
                        )
                        _command.emit(
                            MapCommand.ShowMessage(
                                context.getString(R.string.point_published)
                            )
                        )
                    }

                    result.onFailure {
                        editPointUseCase(
                            updatedPoint.copy(
                                syncState = "UPLOAD_FAILED"
                            )
                        )

                        _command.emit(
                            MapCommand.ShowMessage(
                                context.getString(R.string.publication_error)
                            )
                        )
                    }
                }

                if (!updatedPoint.isPublic && authManager.isAuthorized()) {
                    val result = firestoreRepository.uploadPrivatePoint(updatedPoint)
                    result.onSuccess { cloudId ->
                        editPointUseCase(
                            updatedPoint.copy(
                                cloudId = cloudId,
                                syncState = "SYNCED"
                            )
                        )
                    }
                }

                if (weather == null) {
                    _command.emit(
                        MapCommand.ShowMessage(
                            context.getString(R.string.point_added_no_weather)
                        )
                    )

                } else {
                    _command.emit(
                        MapCommand.ShowMessage(
                            context.getString(R.string.point_added_with_weather)
                        )
                    )
                }
            }
        }
    }

    fun updateUserPoint(
        pointId: Int,
        speciesId: Int?,
        userName: String,
        description: String,
        category: String,
        isPublic: Boolean
    ) {

        viewModelScope.launch {

            state.value.userPoints
                .find { it.id == pointId }
                ?.let { old ->

                    val updatedPoint = old.copy(
                        speciesId = speciesId,
                        userName = userName,
                        description = description,
                        category = category,
                        isPublic = isPublic && authManager.isAuthorized()
                    )

                    editPointUseCase(updatedPoint)

                    if (
                        updatedPoint.isPublic &&
                        updatedPoint.cloudId != null &&
                        authManager.isAuthorized()
                    ) {

                        firestoreRepository.updatePoint(updatedPoint)
                    }

                    if (
                        updatedPoint.isPublic &&
                        authManager.isAuthorized()
                    ) {

                        runCatching {
                            firestoreRepository.uploadPoint(updatedPoint)
                        }
                    }
                }
        }
    }

    fun deletePoint(pointId: Int) {
            viewModelScope.launch {

                val point = state.value.userPoints
                    .find { it.id == pointId }

                if (
                    point?.cloudId != null &&
                    authManager.isAuthorized()
                ) {

                    firestoreRepository.deletePoint(point.cloudId)
                }

                deletePointUseCase(pointId)
            }
    }

    fun sharePoint(
        context: Context,
        point: UserPoints,
        speciesName: String
    ) {
        val dateStr = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date(point.timestamp * 1000L))

        val emoji = FloraCategory.fromKey(point.category ?: "")?.emoji ?: "📍"

        // Всегда используем https:// ссылку — она кликабельна везде
        // и открывает приложение через App Links если FloraPoint установлен
        val appLink = "https://rpuh41.github.io/florapoint-privacy/point" +
                "?lat=${point.latitude}" +
                "&lon=${point.longitude}" +
                "&name=${Uri.encode(point.userName)}" +
                "&category=${point.category ?: "custom"}"

        val text = buildString {
            appendLine("$emoji $speciesName")
            if (point.description.isNotBlank()) {
                appendLine("📝 ${point.description}")
            }
            appendLine("🗓 $dateStr")
            appendLine()
            appendLine("📍 https://maps.google.com/?q=${point.latitude},${point.longitude}")
            appendLine()
            append(context.getString(R.string.share_open_in_app, appLink))
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_title))
        )
    }

    private var _deepLinkName = ""
    private var _deepLinkCategory = "custom"

    fun setDeepLinkData(
        name: String,
        category: String
    ) {

        _deepLinkName = name
        _deepLinkCategory = category

        _state.update {
            it.copy(
                deepLinkName = name,
                deepLinkCategory = category
            )
        }
    }

    fun onPublicPointClicked(point: UserPoints) {
        _state.update {
            it.copy(
                bottomSheetMode = BottomSheetMode.ViewPublic(point)
            )
        }
    }

    fun isPublicForeignPoint(point: UserPoints): Boolean {
        return point.isPublic &&
                point.ownerUid != authManager.getCurrentUserId()
    }

    fun togglePointsFilter() {
        _state.update {
            it.copy(
                showOnlyMyPoints = !it.showOnlyMyPoints
            )
        }
    }
    fun loadCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val weather = weatherService.getWeatherData(lat, lon)
            if (weather != null &&
                weather.temperature != null &&
                weather.humidity != null &&
                weather.avgTemp5Days != null &&
                weather.avgHumidity5Days != null
            ) {
                _state.update {
                    it.copy(
                        currentWeather = CurrentWeather(
                            temperature = weather.temperature,
                            humidity = weather.humidity,
                            avgTemp5Days = weather.avgTemp5Days,
                            avgHumidity5Days = weather.avgHumidity5Days
                        )
                    )
                }
            }
        }
    }
}

sealed interface MapCommand {

    data class CenterMapOnPoint(
        val point: UserPoints
    ) : MapCommand

    data class ShowDeleteConfirmation(
        val pointId: Int
    ) : MapCommand

    data class ShowMessage(
        val text: String
    ) : MapCommand
}

sealed interface BottomSheetMode {

    data class Add(
        val latitude: Double,
        val longitude: Double
    ) : BottomSheetMode

    data class Edit(
        val pointId: Int
    ) : BottomSheetMode

    data class ViewPublic(
        val point: UserPoints
    ) : BottomSheetMode
}

data class MapScreenState(

    val userPoints: List<UserPoints> = emptyList(),
    val publicPoints: List<UserPoints> = emptyList(),
    val selectedPointId: Int? = null,
    val currentUserLocation: Pair<Double, Double>? = null,
    val searchQuery: String = "",
    val species: List<Reference> = emptyList(),
    val bottomSheetMode: BottomSheetMode? = null,
    val deepLinkName: String = "",
    val deepLinkCategory: String = "",
    val showOnlyMyPoints: Boolean = true,
    val currentWeather: CurrentWeather? = null
)

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val avgTemp5Days: Double,
    val avgHumidity5Days: Int
)