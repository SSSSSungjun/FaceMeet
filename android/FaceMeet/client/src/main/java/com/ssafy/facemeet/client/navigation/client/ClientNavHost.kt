package com.ssafy.facemeet.client.navigation.client

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ssafy.facemeet.client.navigation.bottom.MainScreenWithBottomNav
import com.ssafy.facemeet.client.ui.chatlist.ChattingListScreen
import com.ssafy.facemeet.client.ui.profile.ProfileScreen


fun NavGraphBuilder.clientNavHost(navController: NavHostController) {

    composable(ClientRoutes.MainMenu.route) {
        MainScreenWithBottomNav(
            mainNavController = navController
        )
    }

    composable(ClientRoutes.Profile.route) {
        ProfileScreen(onHome = {
            navController.navigate(ClientRoutes.MainMenu.route) {
                popUpTo(ClientRoutes.MainMenu.route) { inclusive = false }
                launchSingleTop = true
            }

        }, onMatching = {
            navController.navigate(ClientRoutes.MatchingLoading.route)
        })
    }

    composable(ClientRoutes.MatchingLoading.route){

    }

    composable(
        route = ClientRoutes.Chat.route,
        arguments = listOf(navArgument("matchingId") { type = NavType.StringType })
    ) { backStackEntry ->
        val matchingId = backStackEntry.arguments?.getString("matchingId")
        ChattingScreen(
            matchingId = matchingId.toString()//임시
        )
    }

}