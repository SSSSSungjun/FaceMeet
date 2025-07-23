package com.ssafy.facemeet.client.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.ui.camera.CameraCaptureScreen
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.core.navigation.Routes


fun NavGraphBuilder.registerMainNavigation(navController: NavHostController) {
    // 그대로 둔다
    composable(Routes.Main.route) {
        CameraScreen(
            onNavigateBack = { navController.popBackStack() },
            onLaunchCamera = { navController.navigate(Routes.CameraCapture.route) } // ★ 추가
        )
    }

    // 새 화면만 추가
    composable(Routes.CameraCapture.route) {
        CameraCaptureScreen(
            onCaptured = { uri ->
                // 저장 끝나면 원하는 곳으로 돌아가기 (여기선 Main으로)
                navController.popBackStack()
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }
}