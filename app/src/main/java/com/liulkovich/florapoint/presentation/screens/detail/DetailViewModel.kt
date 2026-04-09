package com.liulkovich.florapoint.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetSpeciesByIdUseCase
import com.liulkovich.florapoint.domain.Reference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(

    private val getSpeciesByIdUseCase: GetSpeciesByIdUseCase,
    private val savedStateHandle: SavedStateHandle

): ViewModel() {
    private val _state = MutableStateFlow(DetailScreenState())
    val state = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<Int>("speciesId")
        if (id != null) {
            getSpeciesByIdUseCase(id)
                .onEach { species ->
                    _state.update { it.copy(species = species) }
                }
                .launchIn(viewModelScope)
        }
    }
}

data class DetailScreenState(
    val species: Reference? = null
)