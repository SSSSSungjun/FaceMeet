package com.ssafy.facemeet.client.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.ui.camera.CameraCaptureScreen
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.CaptureMode
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.client.ui.camera.FaceTestLoadingScreen
import com.ssafy.facemeet.client.ui.camera.FaceTestResultScreen
import com.ssafy.facemeet.client.ui.camera.ShotPreviewScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import toMultipartBodyPart

@Composable
fun ClientNavHost(onNavigateBack: () -> Unit) {
    val nav = rememberNavController()
    val cameraVM: CameraShotViewModel = viewModel()
    val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

    NavHost(
        navController = nav,
        startDestination = ClientRoutes.Setting.route
    ) {
        composable(ClientRoutes.MainMenu.route) {
            MainMenuScreen {
                // 예: 세팅에서 시작한다면
                nav.navigate(ClientRoutes.Setting.route)
            }
        }

        composable(ClientRoutes.Setting.route) {
            // 설정 화면에서 촬영 시작 버튼 클릭 시
            CameraScreen(
                onNavigateBack = { nav.popBackStack() },
                onLaunchCamera = { nav.navigate(ClientRoutes.FrontCamera.route) }
            )
        }

        composable(ClientRoutes.FrontCamera.route) {
            CameraCaptureScreen(
                mode = CaptureMode.FRONT,
                vm = cameraVM,
                onNavigateBack = { nav.popBackStack() },
                onCaptured = { nav.navigate(ClientRoutes.FrontPreview.route) }
            )
        }
        composable(ClientRoutes.FrontPreview.route) {
            ShotPreviewScreen(
                image = cameraVM.front.value?.asImageBitmap(),
                title = "전면 사진에 이상 없으면 다음을 눌러주세요.",
                onRetake = {
                    cameraVM.resetFront()
                    nav.popBackStack(ClientRoutes.FrontCamera.route, false)
                },
                onConfirm = { nav.navigate(ClientRoutes.SideCamera.route) }
            )
        }

        composable(ClientRoutes.SidePreview.route) {
            ShotPreviewScreen(
                image = cameraVM.side.value?.asImageBitmap(),
                title = "옆면 사진에 이상 없으면 다음을 눌러주세요.",
                onRetake = {
                    cameraVM.resetSide()
                    nav.popBackStack(ClientRoutes.SideCamera.route, false)
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

                        nav.navigate(ClientRoutes.FaceTestLoading.route) {
                            popUpTo(ClientRoutes.Setting.route) { inclusive = false }
                        }
                    }
                }

            )
        }

        composable(ClientRoutes.SideCamera.route) {
            CameraCaptureScreen(
                mode = CaptureMode.SIDE,
                vm = cameraVM,
                onNavigateBack = { nav.popBackStack() },
                onCaptured = { nav.navigate(ClientRoutes.SidePreview.route) }
            )
        }

        composable(ClientRoutes.FaceTestLoading.route) {
            FaceTestLoadingScreen(navController = nav)
        }
        composable(ClientRoutes.FaceTestResult.route) {
            FaceTestResultScreen()
        }

    }
}
