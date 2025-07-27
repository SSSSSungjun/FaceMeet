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
            onLaunchCamera = {
                navController.navigate(SettingRoutes.FrontCamera.route)
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

//    composable(ClientRoutes.FrontPreview.route) {
//        ShotPreviewScreen(
//            image = cameraVM.front.value?.asImageBitmap(),
//            title = "전면 사진에 이상 없으면 다음을 눌러주세요.",
//            onRetake = {
//                cameraVM.resetFront()
//                nav.popBackStack(ClientRoutes.FrontCamera.route, false)
//            },
//            onConfirm = { nav.navigate(ClientRoutes.SideCamera.route) }
//        )
//    }
//
//    composable(ClientRoutes.SidePreview.route) {
//        ShotPreviewScreen(
//            image = cameraVM.side.value?.asImageBitmap(),
//            title = "옆면 사진에 이상 없으면 다음을 눌러주세요.",
//            onRetake = {
//                cameraVM.resetSide()
//                nav.popBackStack(ClientRoutes.SideCamera.route, false)
//            },
//            onConfirm = {
//                Log.d("FlowCheck", "✅ onConfirm 호출됨")
//
//                if (cameraVM.readyBoth()) {
//                    Log.d("FlowCheck", "✅ front & side 준비 완료")
//
//                    val front = cameraVM.front.value!!
//                    val side = cameraVM.side.value!!
//
//                    val frontPart = front.toMultipartBodyPart("image1")
//                    val sidePart = side.toMultipartBodyPart("side_image1")
//                    val userId = "user123".toRequestBody("text/plain".toMediaTypeOrNull())
//
//                    Log.d("FlowCheck", "✅ Multipart 생성 완료")
//
//                    analyzeVM.analyzeFace(frontPart, sidePart, userId)
//
//                    nav.navigate(ClientRoutes.FaceTestLoading.route) {
//                        popUpTo(ClientRoutes.Setting.route) { inclusive = false }
//                    }
//                }
//            }
//
//        )
//    }
//
//    composable(ClientRoutes.SideCamera.route) {
//        CameraCaptureScreen(
//            mode = CaptureMode.SIDE,
//            vm = cameraVM,
//            onNavigateBack = { nav.popBackStack() },
//            onCaptured = { nav.navigate(ClientRoutes.SidePreview.route) }
//        )
//    }
//
//    composable(ClientRoutes.FaceTestLoading.route) {
//        FaceTestLoadingScreen(navController = nav)
//    }
//    composable(ClientRoutes.FaceTestResult.route) {
//        FaceTestResultScreen()
//    }



}
