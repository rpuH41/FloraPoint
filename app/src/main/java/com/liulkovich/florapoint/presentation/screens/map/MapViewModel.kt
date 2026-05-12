package com.liulkovich.florapoint.presentation.screens.map

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.AddNewPointUseCase
import com.liulkovich.florapoint.domain.DeletePointUseCase
import com.liulkovich.florapoint.domain.EditPointUseCase
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetAllUserPointsUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun addNewPoint(
        latitude: Double,
        longitude: Double,
        speciesId: Int?,
        userName: String,
        description: String,
        category: String
    ) {
        val newPoint = UserPoints(
            id = 0,
            speciesId = speciesId,
            latitude = latitude,
            longitude = longitude,
            userName = userName.ifBlank { "Неизвестный вид" },
            description = description,
            category = category,
            timestamp = System.currentTimeMillis() / 1000,
            isFavorite = 0,
            photoPath = "",
            accuracy = 0
        )
        viewModelScope.launch { addNewPointUseCase(newPoint) }
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

        val emoji = when (point.category) {
            "mushroom" -> "🍄"
            "berry"    -> "🫐"
            "plant"    -> "🌿"
            "nut"      -> "🌰"
            else       -> "📍"
        }

        val text = buildString {
            appendLine("$emoji $speciesName")
            if (point.description.isNotBlank()) {
                appendLine("📝 ${point.description}")
            }
            appendLine("🗓 $dateStr")
            appendLine()
            appendLine("Открыть в FloraPoint:")
            appendLine("florapoint://point?lat=${point.latitude}&lon=${point.longitude}&name=${point.userName}&category=${point.category ?: "custom"}")
            appendLine()
            append("📍 https://maps.google.com/?q=${point.latitude},${point.longitude}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться точкой"))
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
    val bottomSheetMode: BottomSheetMode? = null
)