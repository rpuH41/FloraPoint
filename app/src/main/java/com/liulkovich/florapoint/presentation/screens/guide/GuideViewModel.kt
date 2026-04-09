package com.liulkovich.florapoint.presentation.screens.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetAllSpeciesUseCase
import com.liulkovich.florapoint.domain.GetByCategoriesAndNameUseCase
import com.liulkovich.florapoint.domain.GetSpeciesByCategoryUseCase
import com.liulkovich.florapoint.domain.GetSpeciesByNameUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UpdateNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val getSpeciesByCategoryUseCase: GetSpeciesByCategoryUseCase,
    private val getSpeciesByNameUseCase: GetSpeciesByNameUseCase,
    private val getSpeciesByCategoriesAndNameUseCase: GetByCategoriesAndNameUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase
): ViewModel() {

    private val query = MutableStateFlow("")
    private val _state = MutableStateFlow(GuideScreenState())
    val state = _state.asStateFlow()
    private val selectedCategories = MutableStateFlow<Set<String>>(emptySet())


    init {
        combine(query, selectedCategories) { input, categories ->
            input to categories
        }
            .onEach { (input, _) ->
                _state.update { it.copy(query = input) }
            }
            .flatMapLatest { (input, categories) ->
                when {
                    input.isNotBlank() && categories.isNotEmpty() ->
                        getSpeciesByCategoriesAndNameUseCase(categories, input)
                    input.isNotBlank() -> getSpeciesByNameUseCase(input)
                    categories.isNotEmpty() -> getSpeciesByCategoryUseCase(categories)
                    else -> getAllSpeciesUseCase()
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
                    selectedCategories.update { current ->
                        if (command.check in current)
                            current - command.check
                        else
                            current + command.check
                    }
                }
                is GuideCommand.ToggleNotification -> {
                    updateNotificationUseCase(command.id, command.enabled)
                }
            }
        }
    }
}

sealed interface GuideCommand {

    data class InputSearchQuery(val query: String): GuideCommand

    data class CheckCategory(val check: String): GuideCommand

    data class ToggleNotification(val id: Int, val enabled: Int): GuideCommand
}
data class GuideScreenState(
    val query: String = "",
    val species: List<Reference> = listOf()
)