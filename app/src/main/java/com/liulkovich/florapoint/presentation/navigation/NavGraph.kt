package com.liulkovich.florapoint.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.gms.location.LocationServices
import com.liulkovich.florapoint.presentation.components.BottomBar
import com.liulkovich.florapoint.presentation.screens.detail.DetailScreen
import com.liulkovich.florapoint.presentation.screens.detail.DetailViewModel
import com.liulkovich.florapoint.presentation.screens.forecast.ForecastScreen
import com.liulkovich.florapoint.presentation.screens.guide.GuideScreen
import com.liulkovich.florapoint.presentation.screens.home.HomeScreen
import com.liulkovich.florapoint.presentation.screens.map.MapFocusRequestHolder
import com.liulkovich.florapoint.presentation.screens.map.MapScreen
import com.liulkovich.florapoint.presentation.screens.notifications.NotificationScreen
import com.liulkovich.florapoint.presentation.screens.settings.DownloadAreaScreen
import com.liulkovich.florapoint.presentation.screens.settings.OfflineRegionsScreen
import com.liulkovich.florapoint.presentation.screens.settings.SettingsScreen
import com.liulkovich.florapoint.presentation.weather.WeatherViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current
    val weatherViewModel: WeatherViewModel = hiltViewModel()
    val mapFocusHolder: MapFocusRequestHolder = hiltViewModel()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    weatherViewModel.updateLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.updateLocation(location.latitude, location.longitude)
                    }
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    val screensWithoutBottomBar = listOf(Screen.Detail.rout)
    val showBottomBar = currentRoute !in screensWithoutBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute ?: Screen.Home.rout,
                    onNavigate = { route ->
                        if (currentRoute == Screen.Forecast.rout) {
                            navController.popBackStack()
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onForecastClick = {
                        navController.navigate(Screen.Forecast.rout) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.rout,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.rout) {
                HomeScreen(
                    onClickMap = { navController.navigate(Screen.Map.rout) },
                    onClickCategory = { category ->
                        navController.navigate(Screen.Guide.createRoute(category))
                    },
                    onClickDetail = { navController.navigate("Detail/$it") }
                )
            }

            composable(
                route = "Guide?category={category}",
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) {
                GuideScreen(
                    onClickDetail = { reference ->
                        navController.navigate("Detail/${reference.id}")
                    }
                )
            }

            composable(Screen.Map.rout) {
                MapScreen(
                    weatherViewModel = weatherViewModel,
                    mapFocusHolder = mapFocusHolder,
                    onOpenSettings = { navController.navigate(Screen.Settings.rout) }
                )
            }

            composable(
                route = Screen.MapDeepLink.rout,
                arguments = listOf(
                    navArgument("lat") {
                        type = NavType.StringType
                        defaultValue = "0.0"
                    },
                    navArgument("lon") {
                        type = NavType.StringType
                        defaultValue = "0.0"
                    },
                    navArgument("name") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = "custom"
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "florapoint://point?lat={lat}&lon={lon}&name={name}&category={category}"
                    },
                    navDeepLink {
                        uriPattern = "https://rpuh41.github.io/forestpoint-privacy/point?lat={lat}&lon={lon}&name={name}&category={category}"
                    }
                )
//                deepLinks = listOf(
//                    navDeepLink {
//                        uriPattern =
//                            "florapoint://point?lat={lat}&lon={lon}&name={name}&category={category}"
//                    }
//                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: "custom"

                MapScreen(
                    weatherViewModel = weatherViewModel,
                    deepLinkLat = lat,
                    deepLinkLon = lon,
                    deepLinkName = name,
                    deepLinkCategory = category,
                    mapFocusHolder = mapFocusHolder,
                    onOpenSettings = { navController.navigate(Screen.Settings.rout) }
                )
            }

            composable(Screen.Settings.rout) {
                SettingsScreen(
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.rout)
                    },
                    onNavigateToOfflineRegions = {
                        navController.navigate(Screen.OfflineRegions.rout)
                    }
                )
            }

            composable(Screen.Notifications.rout) {
                NotificationScreen()
            }

            composable(
                route = Screen.Detail.rout,
                arguments = listOf(navArgument("speciesId") { type = NavType.IntType })
            ) {
                val viewModel = hiltViewModel<DetailViewModel>()
                DetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNotificationToggle = { enabled ->
                        viewModel.toggleNotification(enabled)
                    },
                    onSpeciesClick = { id ->
                        navController.navigate("Detail/$id")
                    }
                )
            }
            composable(Screen.OfflineRegions.rout) {
                OfflineRegionsScreen(
                    onNavigateToDownload = {
                        navController.navigate(Screen.DownloadArea.rout)
                    }
                )
            }

            composable(Screen.DownloadArea.rout) {
                DownloadAreaScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Forecast.rout) {
                val weather by weatherViewModel.currentWeather.collectAsStateWithLifecycle()
                val location by weatherViewModel.currentLocation.collectAsStateWithLifecycle()

                ForecastScreen(
                    currentWeather = weather,
                    currentLat = location?.first ?: 0.0,
                    currentLon = location?.second ?: 0.0,
                    onNavigateToDetail = { speciesId ->
                        navController.navigate("Detail/$speciesId")
                    },
                    onNavigateToMapWithPoint = { pointId ->
                        if (pointId != null) {
                            mapFocusHolder.request(pointId)
                        }
                        navController.navigate(Screen.Map.rout)
                    }
                )
            }
        }
    }
}

sealed class Screen(val rout: String) {
    data object Home : Screen("Home")
    data object Guide : Screen("Guide?category={category}") {
        fun createRoute(category: String = "") =
            if (category.isEmpty()) "Guide" else "Guide?category=$category"
    }

    data object Map : Screen("Map")
    data object MapDeepLink : Screen("Map?lat={lat}&lon={lon}&name={name}&category={category}")
    data object Settings : Screen("Settings")
    data object Notifications : Screen("Notifications")
    data object Detail : Screen("Detail/{speciesId}")

    data object OfflineRegions : Screen("OfflineRegions")
    data object DownloadArea : Screen("DownloadArea")

    data object Forecast : Screen("Forecast")
}