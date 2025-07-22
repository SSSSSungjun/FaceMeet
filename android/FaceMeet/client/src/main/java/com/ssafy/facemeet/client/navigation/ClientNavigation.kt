package com.ssafy.facemeet.client.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.mainmenu.MainMenuScreen
import com.ssafy.facemeet.core.navigation.Routes


fun NavGraphBuilder.registerMainNavigation(navController: NavHostController) {
    composable(Routes.Main.route) {
        MainMenuScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}