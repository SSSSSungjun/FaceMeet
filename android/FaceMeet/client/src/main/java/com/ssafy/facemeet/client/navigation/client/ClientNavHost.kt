package com.ssafy.facemeet.client.navigation.client

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.bottom.MainScreenWithBottomNav
import com.ssafy.facemeet.client.ui.profile.ProfileScreen

fun NavGraphBuilder.clientNavHost(navController: NavHostController) {

    composable(ClientRoutes.MainMenu.route) {
        MainScreenWithBottomNav(
            mainNavController = navController
        )
    }

    composable(ClientRoutes.Profile.route){
        ProfileScreen()
    }
}