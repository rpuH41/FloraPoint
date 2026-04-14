package com.liulkovich.florapoint.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetSpeciesByIdUseCase
import com.liulkovich.florapoint.domain.Reference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

    private val getSpeciesByIdUseCase: GetSpeciesByIdUseCase,
    private val getAllSpeciesUseCase: GetAllSpeciesUseCase

): ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        loadSpecies()
    }

    private fun loadSpecies() {
        viewModelScope.launch {
            getAllSpeciesUseCase()
                .collect { species ->
                    val currentMonth = java.util.Calendar.getInstance()
                        .get(java.util.Calendar.MONTH) + 1

                    val filtered = species.filter { item ->
                        if (item.startMonth <= item.endMonth) {
                            currentMonth in item.startMonth..item.endMonth
                        } else {
                            currentMonth >= item.startMonth || currentMonth <= item.endMonth
                        }
                    }

                    _state.update { it.copy(species = filtered) }
                }
        }
    }
}

data class HomeScreenState(
    val species: List<Reference> = listOf()
    //val species: Reference? = null,
)