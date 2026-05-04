package com.liulkovich.florapoint.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.data.worker.NotificationPreferences
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UpdateNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsState(
    val notifyStart: Boolean = true,
    val notifyPeak: Boolean = true,
    val notifyEnd: Boolean = true,
    val enabledSpecies: List<Reference> = emptyList()
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val prefs: NotificationPreferences,
    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase
) : ViewModel() {

    val state: StateFlow<NotificationSettingsState> = combine(
        prefs.notifyStart,
        prefs.notifyPeak,
        prefs.notifyEnd,
        getAllSpeciesUseCase()
    ) { start, peak, end, allSpecies ->
        NotificationSettingsState(
            notifyStart = start,
            notifyPeak = peak,
            notifyEnd = end,
            enabledSpecies = allSpecies.filter { it.isNotifEnabled == 1 }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationSettingsState()
    )

    fun setNotifyStart(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifyStart(enabled) }
    }

    fun setNotifyPeak(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifyPeak(enabled) }
    }

    fun setNotifyEnd(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifyEnd(enabled) }
    }

    fun toggleNotification(id: Int, enabled: Int) {
        viewModelScope.launch { updateNotificationUseCase(id, enabled) }
    }
}