package com.ssafy.facemeet.client.navigation

import androidx.compose.compiler.plugins.kotlin.EmptyFunctionMetrics.composable
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.mainmenu.MainMenuScreen

@Composable
fun ClientNavHost(onNavigateBack: () -> Unit) {
    val clientNavController = rememberNavController()

    NavHost(
        navController = clientNavController,
        startDestination = ClientRoutes.MainMenu.route
    ) {
        composable(ClientRoutes.MainMenu.route) {
            MainMenuScreen(
            ){
                //추후 네비 선언
            }
        }


    }
}