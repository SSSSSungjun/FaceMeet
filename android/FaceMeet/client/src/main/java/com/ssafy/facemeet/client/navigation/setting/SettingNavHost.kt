// client/navigation/SettingNavHost.kt
package com.ssafy.facemeet.client.navigation.setting

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.ui.camera.CameraCaptureScreen
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.CaptureMode
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.client.ui.camera.FaceTestLoadingScreen
import com.ssafy.facemeet.client.ui.camera.ShotPreviewScreen
import com.ssafy.facemeet.client.ui.map.MapScreen
import com.ssafy.facemeet.client.ui.register.RegisterScreen

private const val TAG = "SettingNavHost"

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.settingNavHost(
    navController: NavHostController,
    cameraVM: CameraShotViewModel,
    analyzeVM: FaceAnalyzeViewModel,
) {

    composable(
        SettingRoutes.Register.route,
    ) {
        RegisterScreen(
            toNext = {
                navController.navigate(SettingRoutes.CameraStart.route)
            }, // 여기서 뒤로가면 등록 못가게 해야할듯.
            toBack = { navController.popBackStack() },
            toMap = { navController.navigate(SettingRoutes.Map.route) }
        )
    }

    composable(SettingRoutes.Map.route) {
        MapScreen(
            toBack = {
                navController.popBackStack()
            },
            toAccept = {
                navController.popBackStack()
            },
        )
    }
    composable(SettingRoutes.CameraStart.route) {
        CameraScreen(
            onLaunchCamera = { navController.navigate(SettingRoutes.FrontCamera.route) })
    }

    composable(SettingRoutes.FrontCamera.route) {
        CameraCaptureScreen(
            mode = CaptureMode.FRONT,
            vm = cameraVM,
            onCaptured = { navController.navigate(SettingRoutes.FrontPreview.route) },
            onNavigateToBack = { navController.popBackStack() },

            )
    }


    composable(SettingRoutes.FrontPreview.route) {
        Log.d("FACE_MY", "settingNavHost: ${cameraVM.front.value}")
        ShotPreviewScreen(
            image = cameraVM.front.value?.asImageBitmap(),
            title = "전면 사진에 이상 없으면 다음을 눌러주세요.",
            onRetake = {
                cameraVM.resetFront()
                navController.popBackStack(SettingRoutes.FrontCamera.route, false)
            },
            onConfirm = { navController.navigate(SettingRoutes.SideCamera.route) })
    }

    composable(SettingRoutes.SidePreview.route) {
        ShotPreviewScreen(
            image = cameraVM.side.value?.asImageBitmap(),
            title = "옆면 사진에 이상 없으면 다음을 눌러주세요.",
            onRetake = {
                cameraVM.resetSide()
                navController.popBackStack(SettingRoutes.SideCamera.route, false)
            },
            onConfirm = {
                if (cameraVM.readyBoth()) {
                    navController.navigate(SettingRoutes.FaceTestLoading.route) {
                        popUpTo(ClientRoutes.Profile.route) { inclusive = false }
                    }
                }
            }

        )
    }

    composable(SettingRoutes.SideCamera.route) {
        CameraCaptureScreen(
            mode = CaptureMode.SIDE,
            vm = cameraVM,
            onNavigateToBack = { navController.popBackStack() },
            onCaptured = { navController.navigate(SettingRoutes.SidePreview.route) })
    }

    composable(SettingRoutes.FaceTestLoading.route) {
        FaceTestLoadingScreen(
            cameraShotViewModel = cameraVM,
            analyzeViewModel = analyzeVM,
            onNavigateToResult = {
                navController.navigate(ClientRoutes.MainMenu.route) {
                    popUpTo(0) { inclusive = true } // 백스택 싹 날림
                }
                navController.navigate(ClientRoutes.Profile.route)
            },
            retry = {
                navController.navigate(SettingRoutes.CameraStart.route) {
                    popUpTo(SettingRoutes.CameraStart.route) { inclusive = false }
                }
            }, onCancel = {
                navController.popBackStack()
            })
    }
}
