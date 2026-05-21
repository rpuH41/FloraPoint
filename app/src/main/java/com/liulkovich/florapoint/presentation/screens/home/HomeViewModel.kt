package com.liulkovich.florapoint.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetRandomTipUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.domain.UpdateNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    private val getRandomTipUseCase: GetRandomTipUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase

): ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        getAllSpeciesUseCase()
            .onEach { species ->
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val filtered = species.filter { item ->
                    item.category != "other" &&
                            if (item.startMonth <= item.endMonth) {
                                currentMonth in item.startMonth..item.endMonth
                            } else {
                                currentMonth >= item.startMonth || currentMonth <= item.endMonth
                            }
                }.sortedBy { item ->
                    val today = Calendar.getInstance()
                    val endCal = Calendar.getInstance().apply {
                        set(Calendar.MONTH, item.endMonth - 1)
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        if (before(today)) add(Calendar.YEAR, 1)
                    }
                    endCal.timeInMillis - today.timeInMillis
                }
                _state.update { it.copy(species = filtered, isLoading = false) }
            }
            .launchIn(viewModelScope)
        loadRandomTip()
    }

    private fun loadRandomTip() {
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

}

data class HomeScreenState(
    val species: List<Reference> = listOf(),
    //val species: Reference? = null,
    val tip: Tip? = null,
    val isLoading: Boolean = true,
    val isTipLoading: Boolean = true
)