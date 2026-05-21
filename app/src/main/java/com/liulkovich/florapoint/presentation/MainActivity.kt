package com.liulkovich.florapoint.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.liulkovich.florapoint.presentation.navigation.NavGraph
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import com.liulkovich.florapoint.presentation.screens.home.HomeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val homeViewModel: HomeViewModel by viewModels()


    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        splashScreen.setKeepOnScreenCondition {
            val state = homeViewModel.state.value
            state.isLoading || state.isTipLoading
        }

        setContent {
            FloraPointTheme {
                NavGraph()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}