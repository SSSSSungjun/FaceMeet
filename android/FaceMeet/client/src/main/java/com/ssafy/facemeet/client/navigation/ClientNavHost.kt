package com.ssafy.facemeet.client.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen
import com.ssafy.facemeet.core.navigation.Routes

@Composable
fun ClientNavHost(onNavigateBack: () -> Unit) {
    val clientNavController = rememberNavController()

    NavHost(
        navController = clientNavController,
        startDestination = ClientRoutes.Setting.route //임시
    ) {
        composable(ClientRoutes.MainMenu.route) {
            MainMenuScreen(
            ) {
                //추후 네비 선언
            }
        }

        // 새 화면만 추가
        composable(ClientRoutes.Setting.route) {
            CameraScreen(
                onNavigateBack = { clientNavController.popBackStack() },
                onLaunchCamera = { clientNavController.navigate(Routes.CameraCapture.route) } // ★ 추가
            )
        }


    }
}