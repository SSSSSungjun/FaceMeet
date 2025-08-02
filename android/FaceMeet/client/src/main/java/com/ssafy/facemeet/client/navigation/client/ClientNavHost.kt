package com.ssafy.facemeet.client.navigation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.bottom.MainScreenWithBottomNav
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.ui.chat.ChattingScreen
import com.ssafy.facemeet.client.ui.matching.MatchingLoadingScreen
import com.ssafy.facemeet.client.ui.notification.NotificationScreen
import com.ssafy.facemeet.client.ui.profile.ProfileScreen


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.clientNavHost(navController: NavHostController) {

    composable(ClientRoutes.MainMenu.route) {
        MainScreenWithBottomNav(
            mainNavController = navController
        )
    }

    composable(ClientRoutes.Notification.route) {
        NotificationScreen(onBackClick = { navController.popBackStack() })
    }

    composable(ClientRoutes.Profile.route) {
        ProfileScreen(onHome = {
            navController.navigate(ClientRoutes.MainMenu.route) {
                popUpTo(ClientRoutes.MainMenu.route) { inclusive = false }
                launchSingleTop = true
            }
        }, onMatching = {
            navController.navigate(ClientRoutes.MatchingLoading.route)
        }, onRetry = {
            navController.navigate(SettingRoutes.CameraStart.route)
        })
    }

    composable(ClientRoutes.MatchingLoading.route) {
        MatchingLoadingScreen()
    }

    composable(ClientRoutes.Chat.route,) {
        ChattingScreen(
            userId = 95,
            roomId = 7,
            receiverId = 97,
            onBackClick = {}
        )
    }

}