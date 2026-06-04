package com.liulkovich.florapoint.presentation.screens.settings

import android.content.Context
import android.content.Intent
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
import com.liulkovich.florapoint.domain.cloud.FirestoreRepository
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

import com.liulkovich.florapoint.presentation.auth.AuthManager
import kotlinx.coroutines.delay

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportPointsUseCase: ExportPointsUseCase,
    private val importPointsUseCase: ImportPointsUseCase,
    private val tileDownloadManager: TileDownloadManager,
    private val repository: FloraRepository,
    private val authManager: AuthManager,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    val offlineRegions = repository.getAllOfflineRegions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        _authState.value = AuthState(
            isAuthorized = authManager.isAuthorized(),
            userName = authManager.getCurrentUserName(),
            userEmail = authManager.getCurrentUserEmail()
        )
    }
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
    fun isAuthorized(): Boolean {
        return authManager.isAuthorized()
    }

    fun getUserName(): String {
        return authManager.getCurrentUserName()
            ?: "Google User"
    }

    fun getUserEmail(): String {
        return authManager.getCurrentUserEmail()
            ?: ""
    }

    fun logout() {
        authManager.logout()
        observeAuthState()
    }


    fun getGoogleSignInIntent(context: Context): Intent {
        return authManager.getGoogleSignInIntent(context)
    }

    fun handleGoogleSignInResult(
        intent: Intent?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authManager.handleGoogleSignInResult(
            intent,
            onSuccess = { anonymousUid ->
                observeAuthState()
                viewModelScope.launch {
                    val newUid = authManager.getCurrentUserId() ?: return@launch

                    if (anonymousUid != null) {
                        val localPoints = repository.getAllUserPointsList()
                        localPoints.forEach { point ->
                            if (point.ownerUid == anonymousUid || point.ownerUid == null) {
                                val updatedPoint = point.copy(ownerUid = newUid)
                                repository.editPoint(updatedPoint)
                                if (updatedPoint.isPublic) {
                                    firestoreRepository.uploadPoint(updatedPoint)
                                } else {
                                    firestoreRepository.uploadPrivatePoint(updatedPoint)
                                }
                            }
                        }
                    }
                    val publicPoints = firestoreRepository.downloadUserPoints(newUid)
                    val privatePoints = firestoreRepository.downloadPrivatePoints(newUid)
                    (publicPoints + privatePoints).forEach { point ->
                        val existing = repository.getPointByCloudId(point.cloudId ?: return@forEach)
                        if (existing == null) {
                            repository.insertPoint(point)
                        }
                    }
                }
                onSuccess()
            },
            onError = onError
        )
    }
}
data class AuthState(
    val isAuthorized: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null
)