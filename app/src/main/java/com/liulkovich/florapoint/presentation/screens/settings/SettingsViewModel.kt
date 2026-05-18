package com.liulkovich.florapoint.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liulkovich.florapoint.data.DownloadProgress
import com.liulkovich.florapoint.data.TileDownloadManager
import com.liulkovich.florapoint.domain.ExportFormat
import com.liulkovich.florapoint.domain.ExportPointsUseCase
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.ImportPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportPointsUseCase: ExportPointsUseCase,
    private val importPointsUseCase: ImportPointsUseCase,
    private val tileDownloadManager: TileDownloadManager,
    private val repository: FloraRepository
) : ViewModel() {

    val offlineRegions = repository.getAllOfflineRegions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun downloadArea(
        context: Context,
        boundingBox: BoundingBox,
        regionName: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val tempMapView = MapView(context)

            try {
                launch(Dispatchers.IO) {
                    tileDownloadManager.downloadTiles(
                        context = context,
                        boundingBox = boundingBox,
                        regionName = regionName,
                        mapView = tempMapView,
                        onProgress = { progress ->
                            _downloadProgress.value = progress
                        }
                    )
                }
            } finally {
                tempMapView.onDetach()
            }
        }
    }

    fun deleteRegion(id: String) {
        viewModelScope.launch {
            repository.deleteOfflineRegion(id)
        }
    }

    fun resetDownloadProgress() {
        _downloadProgress.value = null
    }

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

    fun handleImport(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                importPointsUseCase(context, uri, ExportFormat.GPX)
            }
        }
    }

    fun countTiles(boundingBox: BoundingBox): Int =
        tileDownloadManager.countTiles(boundingBox)
}