package com.liulkovich.florapoint.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liulkovich.florapoint.presentation.components.BottomBar
import com.liulkovich.florapoint.presentation.screens.detail.DetailScreen
import com.liulkovich.florapoint.presentation.screens.detail.DetailViewModel
import com.liulkovich.florapoint.presentation.screens.guide.GuideScreen
import com.liulkovich.florapoint.presentation.screens.home.HomeScreen
import com.liulkovich.florapoint.presentation.screens.map.MapScreen
import com.liulkovich.florapoint.presentation.screens.notifications.NotificationScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val screensWithoutBottomBar = listOf(Screen.Detail.rout)
    val showBottomBar = currentRoute !in screensWithoutBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute ?: Screen.Home.rout,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.rout) {
                                saveState = true
                                inclusive = route == Screen.Home.rout
                            }
                            launchSingleTop = true
                            restoreState = route != Screen.Home.rout
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
                    onClickMap = {
                        navController.navigate(Screen.Map.rout)
                    },
                    onClickCategory = { category ->
                        navController.navigate(Screen.Guide.createRoute(category))
                    },
                    onClickDetail = {
                        navController.navigate("Detail/$it")
                    }
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
    data object Notifications : Screen("Notifications")
    data object Detail : Screen("Detail/{speciesId}")
}
