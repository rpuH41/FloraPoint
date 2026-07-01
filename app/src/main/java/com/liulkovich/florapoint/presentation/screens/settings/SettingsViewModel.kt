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
import com.liulkovich.florapoint.presentation.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import javax.inject.Inject
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    private val _currentLanguage = MutableStateFlow(
        AppCompatDelegate.getApplicationLocales().let {
            if (it.isEmpty) "system" else it[0]?.language ?: "system"
        }
    )
    val currentLanguage = _currentLanguage.asStateFlow()

    fun setLanguage(languageTag: String) {
        val locales = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(locales)
        _currentLanguage.value = languageTag
    }

    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "system"
        else locales[0]?.language ?: "system"
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = authManager.getCurrentUserId() ?: return@launch
                firestoreRepository.deleteAllUserPoints(uid)
                firestoreRepository.deleteAllPrivatePoints(uid)
                authManager.deleteAccount(
                    context = context,
                    onSuccess = {
                        observeAuthState()
                        onSuccess()
                    },
                    onError = onError
                )
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка удаления")
            }
        }
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

                    val allLocalPoints = repository.getAllUserPointsList()

                    val pointsToSync = allLocalPoints.filter { point ->
                        (point.ownerUid == anonymousUid ||
                                point.ownerUid == newUid ||
                                point.ownerUid == null) &&
                                (point.cloudId == null || point.syncState != "SYNCED")
                    }

                    pointsToSync.forEach { point ->
                        val pointWithUid = point.copy(ownerUid = newUid)
                        if (pointWithUid.isPublic) {
                            val result = firestoreRepository.uploadPoint(pointWithUid)
                            result.onSuccess { cloudId ->
                                repository.editPoint(
                                    pointWithUid.copy(cloudId = cloudId, syncState = "SYNCED")
                                )
                            }
                            result.onFailure {
                                repository.editPoint(pointWithUid)
                            }
                        } else {
                            val result = firestoreRepository.uploadPrivatePoint(pointWithUid)
                            result.onSuccess { cloudId ->
                                repository.editPoint(
                                    pointWithUid.copy(cloudId = cloudId, syncState = "SYNCED")
                                )
                            }
                            result.onFailure {
                                repository.editPoint(pointWithUid)
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