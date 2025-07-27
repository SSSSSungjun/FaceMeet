// client/navigation/SettingNavHost.kt
package com.ssafy.facemeet.client.navigation.setting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.client.ui.register.RegisterScreen

private const val TAG = "ClientNavigation"

fun NavGraphBuilder.settingNavHost(
    navController: NavHostController
) {
    // 정보 기입 화면
    composable(SettingRoutes.Register.route) {
        val source = navController.previousBackStackEntry?.destination?.route

        RegisterScreen(
            onNavigateToNext = {
                if (source == ClientRoutes.Profile.route)
                    navController.navigate(SettingRoutes.CameraStart.route) {
                        popUpTo(SettingRoutes.Register.route) { inclusive = true }
                    }
                else
                    navController.navigate(SettingRoutes.CameraStart.route)
            },
            onNavigateToBack = {
                navController.popBackStack()
            }
        )
    }

    // 카메라 촬영 안내 화면
    composable(SettingRoutes.CameraStart.route) {
        CameraScreen(
            onNavigateToNext = {

            },
            onNavigateToBack = {}
        )
    }


}
