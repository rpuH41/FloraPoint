package com.liulkovich.florapoint.presentation.screens.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapFocusRequestHolder @Inject constructor() : ViewModel() {

    private val _pendingFocusPointId = MutableStateFlow<Int?>(null)
    val pendingFocusPointId = _pendingFocusPointId.asStateFlow()

    fun request(pointId: Int) {
        _pendingFocusPointId.value = pointId
    }

    fun consume() {
        _pendingFocusPointId.value = null
    }
}
