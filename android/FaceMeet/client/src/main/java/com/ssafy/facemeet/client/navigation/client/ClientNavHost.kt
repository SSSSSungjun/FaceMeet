package com.ssafy.facemeet.client.navigation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ssafy.facemeet.client.navigation.bottom.BottomNavRoutes
import com.ssafy.facemeet.client.navigation.bottom.MainScreenWithBottomNav
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.ui.chat.ChattingScreen
import com.ssafy.facemeet.client.ui.matching.MatchingLoadingScreen
import com.ssafy.facemeet.client.ui.notification.NotificationScreen
import com.ssafy.facemeet.client.ui.profile.ProfileScreen
import com.ssafy.facemeet.client.ui.profile.partner.PartnerProfileScreen


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.clientNavHost(
    navController: NavHostController,
    bottomNavController: NavHostController
) {

    composable(ClientRoutes.MainMenu.route) {
        MainScreenWithBottomNav(
            mainNavController = navController,
            bottomNavController = bottomNavController
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
        MatchingLoadingScreen(
            onCancel = { navController.popBackStack() },
            onMatchFound = { matchedChatRoomId ->

                // 2. 채팅리스트 밑에 깔고,
                bottomNavController.navigate(BottomNavRoutes.ChattingList.route) {
                    popUpTo(BottomNavRoutes.Matching.route) { inclusive = true }
                    launchSingleTop = true
                }

                // 3. 그 다음 Chat 진입
                navController.navigate(ClientRoutes.Chat.createRoute(matchedChatRoomId, -1L)) {
                    popUpTo(ClientRoutes.MatchingLoading.route) { inclusive = true } // 로딩까지 제거
                    launchSingleTop = true
                }
            }


        )
    }


    composable(
        route = ClientRoutes.Chat.route,
        arguments = listOf(
            navArgument("roomId") {
                type = NavType.LongType
                defaultValue = 0L // 기본값 설정 가능
            },
            navArgument("receiverId") {
                type = NavType.LongType
                defaultValue = 0L
            }
        )
    ) { backStackEntry ->
        val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0
        val receiverId = backStackEntry.arguments?.getLong("receiverId") ?: 0

        ChattingScreen(
            roomId = roomId,
            receiverId = receiverId,
            onBackClick = {
                navController.popBackStack()
            },
            onPartnerProfile = { partnerId ->
                navController.navigate(ClientRoutes.PartnerProfile.routeWithArgs(partnerId, roomId))
            }
        )
    }

    // navGraph
    composable(
        route = "${ClientRoutes.PartnerProfile.route}/{partnerId}/{roomId}",
        arguments = listOf(
            navArgument("partnerId") { type = NavType.LongType },
            navArgument("roomId") { type = NavType.LongType }
        )
    ) {
        val partnerId = it.arguments?.getLong("partnerId") ?: return@composable
        val roomId = it.arguments?.getLong("roomId") ?: return@composable

        PartnerProfileScreen(
            partnerId = partnerId,
            roomId = roomId,
            onBack = { navController.popBackStack() }
        )
    }


}