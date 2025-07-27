// client/navigation/SettingNavHost.kt
package com.ssafy.facemeet.client.navigation.setting

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.ui.camera.CameraCaptureScreen
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.CaptureMode
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.client.ui.camera.FaceTestLoadingScreen
import com.ssafy.facemeet.client.ui.camera.ShotPreviewScreen
import com.ssafy.facemeet.client.ui.register.RegisterScreen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import toMultipartBodyPart

private const val TAG = "ClientNavigation"

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavBackStackEntry.getCameraViewModels(navController: NavHostController): Pair<CameraShotViewModel, FaceAnalyzeViewModel> {
    val rootEntry = remember(navController) {
        navController.getBackStackEntry(SettingRoutes.CameraStart.route)
    }
    return Pair(
        viewModel(rootEntry),
        hiltViewModel(rootEntry)
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.settingNavHost(
    navController: NavHostController,
) {
    // 정보 기입 화면
    composable(SettingRoutes.Register.route) {
        val source = navController.previousBackStackEntry?.destination?.route

        RegisterScreen( // 여기서 정보기입해야 saveToken을 해야할수도
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

    composable(SettingRoutes.FrontCamera.route) {
        val (cameraVM, _) = it.getCameraViewModels(navController)
        CameraCaptureScreen(
            mode = CaptureMode.FRONT,
            vm = cameraVM,
            onCaptured = { navController.navigate(SettingRoutes.FrontPreview.route) },
            onNavigateToBack = { navController.popBackStack() },

        )
    }

    composable(SettingRoutes.FrontPreview.route) {
        val (cameraVM, _) = it.getCameraViewModels(navController)
        ShotPreviewScreen(
            image = cameraVM.front.value?.asImageBitmap(),
            title = "전면 사진에 이상 없으면 다음을 눌러주세요.",
            onRetake = {
                cameraVM.resetFront()
                navController.popBackStack(SettingRoutes.FrontCamera.route, false)
            },
            onConfirm = { navController.navigate(SettingRoutes.SideCamera.route) }
        )
    }

    composable(SettingRoutes.SidePreview.route) {
        val (cameraVM, analyzeVM) = it.getCameraViewModels(navController)
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
                    val userId = "user123".toRequestBody("text/plain".toMediaTypeOrNull())

                    Log.d("FlowCheck", "✅ Multipart 생성 완료")

                    analyzeVM.analyzeFace(frontPart, sidePart, userId)

                    navController.navigate(SettingRoutes.FaceTestLoading.route) {
                        popUpTo(SettingRoutes.Register.route) { inclusive = false }
                    }
                }
            } // 여기 너무 로직 길다고 생각합니다. 네비게이션인데..

        )
    }

    composable(SettingRoutes.SideCamera.route) {
        val (cameraVM, _) = it.getCameraViewModels(navController)
        CameraCaptureScreen(
            mode = CaptureMode.SIDE,
            vm = cameraVM,
            onNavigateToBack = { navController.popBackStack() },
            onCaptured = { navController.navigate(SettingRoutes.SidePreview.route) }
        )
    }

    composable(SettingRoutes.FaceTestLoading.route) { entry ->
        val (_, analyzeVM) = entry.getCameraViewModels(navController)

        FaceTestLoadingScreen(
            viewModel = analyzeVM,
            onNavigateToResult = {
                navController.navigate(ClientRoutes.Profile.route) {
                    popUpTo(SettingRoutes.Register.route) { inclusive = false }
                }
            }
        )
    }


}