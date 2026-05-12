package com.liulkovich.florapoint.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.domain.ExportFormat
import com.liulkovich.florapoint.domain.ExportPointsUseCase
import com.liulkovich.florapoint.domain.ImportPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportPointsUseCase: ExportPointsUseCase,
    private val importPointsUseCase: ImportPointsUseCase
) : ViewModel() {

    fun exportPoints(context: Context, onResult: (Uri) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val file = exportPointsUseCase(context, ExportFormat.GPX)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                onResult(uri)
            }
        }
    }

    fun importPoints(context: Context) {

    }

    fun handleImport(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                importPointsUseCase(context, uri, ExportFormat.GPX)
            }
        }
    }
}