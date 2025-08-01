// client/navigation/SettingNavHost.kt
package com.ssafy.facemeet.client.navigation.setting

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
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
import toMultipartBodyPart

private const val TAG = "SettingNavHost"

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.settingNavHost(
    navController: NavHostController
) {
    composable(
        SettingRoutes.Register.route,
    ) {
        RegisterScreen(
            toNext = {
                navController.navigate(ClientRoutes.MainMenu.route)
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
        val cameraVM: CameraShotViewModel = hiltViewModel()
        CameraCaptureScreen(
            mode = CaptureMode.FRONT,
            vm = cameraVM,
            onCaptured = { navController.navigate(SettingRoutes.FrontPreview.route) },
            onNavigateToBack = { navController.popBackStack() },

            )
    }

    composable(SettingRoutes.FrontPreview.route) {
        val cameraVM: CameraShotViewModel = hiltViewModel()
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
        val cameraVM: CameraShotViewModel = hiltViewModel()
        val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

        ShotPreviewScreen(
            image = cameraVM.side.value?.asImageBitmap(),
            title = "옆면 사진에 이상 없으면 다음을 눌러주세요.",
            onRetake = {
                cameraVM.resetSide()
                navController.popBackStack(SettingRoutes.SideCamera.route, false)
            },
            onConfirm = {
                Log.d("FlowCheck", "✅ onConfirm 호출됨")

                if (cameraVM.readyBoth()) {
                    Log.d("FlowCheck", "✅ front & side 준비 완료")

                    val front = cameraVM.front.value!!
                    val side = cameraVM.side.value!!

                    val frontPart = front.toMultipartBodyPart("image1")
                    val sidePart = side.toMultipartBodyPart("side_image1")

                    Log.d("FlowCheck", "✅ Multipart 생성 완료")

                    analyzeVM.analyzeFace(frontPart, sidePart)

                    navController.navigate(SettingRoutes.FaceTestLoading.route) {
                        popUpTo(SettingRoutes.Register.route) { inclusive = false }
                    }
                }
            } // 여기 너무 로직 길다고 생각합니다. 네비게이션인데..

        )
    }

    composable(SettingRoutes.SideCamera.route) {
        val cameraVM: CameraShotViewModel = hiltViewModel()
        CameraCaptureScreen(
            mode = CaptureMode.SIDE,
            vm = cameraVM,
            onNavigateToBack = { navController.popBackStack() },
            onCaptured = { navController.navigate(SettingRoutes.SidePreview.route) })
    }

    composable(SettingRoutes.FaceTestLoading.route) {
        val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()
        FaceTestLoadingScreen(
            viewModel = analyzeVM, onNavigateToResult = {
                navController.navigate(ClientRoutes.Profile.route) {
                    popUpTo(SettingRoutes.Register.route) { inclusive = false }
                }
            })
    }
}
