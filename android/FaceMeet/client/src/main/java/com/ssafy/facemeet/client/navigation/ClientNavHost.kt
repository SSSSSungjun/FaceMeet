package com.ssafy.facemeet.client.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.ui.camera.CameraCaptureScreen
import com.ssafy.facemeet.client.ui.camera.CameraScreen
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.CaptureMode
import com.ssafy.facemeet.client.ui.camera.FaceTestLoadingScreen
import com.ssafy.facemeet.client.ui.camera.ShotPreviewScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ClientNavHost(onNavigateBack: () -> Unit) {
    val nav = rememberNavController()
    val vm: CameraShotViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = ClientRoutes.Setting.route // 임시
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
                vm = vm,
                onNavigateBack = { nav.popBackStack() },
                onCaptured = { nav.navigate(ClientRoutes.FrontPreview.route) }
            )
        }
        composable(ClientRoutes.FrontPreview.route) {
            ShotPreviewScreen(
                image = vm.front.value?.asImageBitmap(),
                title = "전면 사진에 이상 없으면 다음을 눌러주세요.",
                onRetake = {
                    vm.resetFront()
                    nav.popBackStack(ClientRoutes.FrontCamera.route, false)
                },
                onConfirm = { nav.navigate(ClientRoutes.SideCamera.route) }
            )
        }

        composable(ClientRoutes.SidePreview.route) {
            ShotPreviewScreen(
                image = vm.side.value?.asImageBitmap(),
                title = "후면 사진에 이상 없으면 다음을 눌러주세요.",
                onRetake = {
                    vm.resetSide()
                    nav.popBackStack(ClientRoutes.SideCamera.route, false)
                },
                onConfirm = {
                    if (vm.readyBoth()) {
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
                vm = vm,
                onNavigateBack = { nav.popBackStack() },
                onCaptured = { nav.navigate(ClientRoutes.SidePreview.route) }
            )
        }


        composable(ClientRoutes.FaceTestLoading.route) {
            FaceTestLoadingScreen()
        }
    }
}
