package com.liulkovich.florapoint.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetSpeciesByNameUseCase
import com.liulkovich.florapoint.domain.Reference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GuideViewModel @Inject constructor(

    private val getAllSpeciesUseCase: GetAllSpeciesUseCase,
    //private val getSpeciesByCategoryUseCase: GetSpeciesByCategoryUseCase,
    private val getSpeciesByNameUseCase: GetSpeciesByNameUseCase
): ViewModel() {

    private val query = MutableStateFlow("")
    private val _state = MutableStateFlow(GuideScreenState())
    val state = _state.asStateFlow()
    private val selectedCategory = MutableStateFlow<String?>(null)

    init {
            query
            .onEach { input ->
                _state.update { it.copy( query = input) }
            }
            .flatMapLatest<String, List<Reference>> { input ->
                if (input.isBlank()){
                    getAllSpeciesUseCase()
                } else {
                    getSpeciesByNameUseCase(input)
                }
            }
            .onEach { speciesList ->
                _state.update { it.copy(species = speciesList) }
            }
            .launchIn(viewModelScope)
    }

    fun processCommand(command: GuideCommand){
        viewModelScope.launch {
            when(command){

                is GuideCommand.InputSearchQuery -> {

                    query.update { command.query.trim() }
                }

                is GuideCommand.CheckCategory -> {
                    selectedCategory.update { command.check }
                }
            }
        }
    }
}

sealed interface GuideCommand {

    data class InputSearchQuery(val query: String): GuideCommand

    data class CheckCategory(val check: String): GuideCommand
}
data class GuideScreenState(
    val query: String = "",
    val species: List<Reference> = listOf(),
    val selectedCategory: String? = null
)