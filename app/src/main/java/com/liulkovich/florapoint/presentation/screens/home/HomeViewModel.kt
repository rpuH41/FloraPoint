package com.liulkovich.florapoint.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetRandomTipUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.domain.UpdateNotificationUseCase
import com.liulkovich.florapoint.domain.cloud.FirestoreRepository
import com.liulkovich.florapoint.presentation.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    private val getRandomTipUseCase: GetRandomTipUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val authManager: AuthManager,
    private val firestoreRepository: FirestoreRepository,
    private val repository: FloraRepository
): ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    private val selectedCategory = MutableStateFlow<String?>(null)

    init {
        loadSpecies()
        loadTips()
    }

    private fun loadSpecies() {
        combine(getAllSpeciesUseCase(), selectedCategory) { species, category ->
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

            species.filter { item ->
                item.category != "other" &&
                        item.isReferenceOnly != 1 &&
                        (category == null || item.category == category) &&
                        isInSeason(item, currentMonth)
            }.sortedBy { item ->
                daysUntilSeasonEnd(item.endMonth, currentMonth)
            }
        }
            .onEach { filtered ->
                _state.update { it.copy(species = filtered, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onSeasonCategorySelected(category: String?) {
        selectedCategory.value = category
        _state.update { it.copy(selectedSeasonCategory = category) }
    }

    private fun loadTips() {
        viewModelScope.launch {
            val tip = getRandomTipUseCase()
            _state.update { it.copy(tip = tip, isTipLoading = false) }
        }
    }
    fun toggleNotification(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationUseCase(id, if (enabled) 1 else 0)
        }
    }

    fun syncPointsFromCloud() {
        viewModelScope.launch {
            val uid = authManager.getCurrentUserId() ?: return@launch
            val publicPoints = firestoreRepository.downloadUserPoints(uid)
            val privatePoints = firestoreRepository.downloadPrivatePoints(uid)
            (publicPoints + privatePoints).forEach { point ->
                val existing = repository.getPointByCloudId(point.cloudId ?: return@forEach)
                if (existing == null) {
                    repository.insertPoint(point)
                }
            }
        }
    }

    private fun isInSeason(item: Reference, currentMonth: Int): Boolean {
        return if (item.startMonth <= item.endMonth) {
            currentMonth in item.startMonth..item.endMonth
        } else {
            currentMonth >= item.startMonth || currentMonth <= item.endMonth
        }
    }

    private fun daysUntilSeasonEnd(endMonth: Int, currentMonth: Int): Int {
        val end = endMonth.coerceIn(1, 12)
        val current = currentMonth.coerceIn(1, 12)

        return if (end >= current) {
            (end - current) * 31
        } else {
            (12 - current + end) * 31
        }
    }
}

data class HomeScreenState(
    val species: List<Reference> = listOf(),
    val tip: Tip? = null,
    val isLoading: Boolean = true,
    val isTipLoading: Boolean = true,
    val selectedSeasonCategory: String? = null
)