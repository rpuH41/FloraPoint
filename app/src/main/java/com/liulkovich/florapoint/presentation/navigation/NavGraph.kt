package com.liulkovich.florapoint.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liulkovich.florapoint.presentation.screens.detail.DetailScreen
import com.liulkovich.florapoint.presentation.screens.detail.DetailViewModel
import com.liulkovich.florapoint.presentation.screens.guide.GuideScreen

@Composable
fun NavGraph(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Guide.rout
    ){
        composable(Screen.Guide.rout) {
            GuideScreen(
                onClickDetail = { reference ->
                    navController.navigate("Detail/${reference.id}")
                }
            )
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
                    viewModel.toggleNotification(enabled)  // ← теперь идёт в базу
                }
            )
        }
    }

}

sealed class Screen(val rout: String){

    data object Guide : Screen(rout = "Guide")

    data object Detail : Screen("Detail/{speciesId}")

}