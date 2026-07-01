package com.liulkovich.florapoint.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.liulkovich.florapoint.presentation.auth.AuthManager
import com.liulkovich.florapoint.presentation.navigation.NavGraph
import com.liulkovich.florapoint.presentation.screens.home.HomeViewModel
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val homeViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var authManager: AuthManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.d("UPDATE", "Update flow failed or cancelled: ${result.resultCode}")
        }
    }

    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        checkForUpdate()

        splashScreen.setKeepOnScreenCondition {
            val state = homeViewModel.state.value
            state.isLoading || state.isTipLoading
        }

        authManager.signInAnonymously(
            onSuccess = {
                Log.d("AUTH", "UID = ${authManager.getCurrentUserId()}")
                if (authManager.isRegistered()) {
                    homeViewModel.syncPointsFromCloud()
                }
            },
            onError = {
                Log.d("AUTH", "ERROR = $it")
            }
        )

        setContent {
            FloraPointTheme {
                NavGraph()
            }
        }
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }.addOnFailureListener {
            Log.d("UPDATE", "Update check failed: $it")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}