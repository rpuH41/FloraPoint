package com.liulkovich.florapoint.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.GetSpeciesByIdUseCase
import com.liulkovich.florapoint.domain.GetSpeciesByExactNameUseCase
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UpdateNotificationUseCase
import com.liulkovich.florapoint.domain.localizedLookAlikes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getSpeciesByIdUseCase: GetSpeciesByIdUseCase,
    private val getSpeciesByExactNameUseCase: GetSpeciesByExactNameUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DetailScreenState())
    val state = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<Int>("speciesId")
        if (id != null) {
            getSpeciesByIdUseCase(id)
                .onEach { species ->
                    _state.update {
                        it.copy(
                            species = species,
                            isNotificationEnabled = species?.isNotifEnabled == 1
                        )
                    }
                    species?.let { resolveLookAlikes(it) }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun resolveLookAlikes(species: Reference) {
        viewModelScope.launch {
            val isRu = Locale.getDefault().language != "en"
            val raw = if (isRu) species.lookAlikesRu else
                species.lookAlikesEn?.takeIf { it.isNotBlank() } ?: species.lookAlikesRu

            val names = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val resolved = names.associateWith { name ->
                getSpeciesByExactNameUseCase(name)?.id
            }

            _state.update { it.copy(resolvedLookAlikes = resolved) }
        }
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            state.value.species?.let { species ->
                updateNotificationUseCase(species.id, if (enabled) 1 else 0)
            }
        }
    }
}

data class DetailScreenState(
    val species: Reference? = null,
    val isNotificationEnabled: Boolean = false,
    val resolvedLookAlikes: Map<String, Int?> = emptyMap()
)