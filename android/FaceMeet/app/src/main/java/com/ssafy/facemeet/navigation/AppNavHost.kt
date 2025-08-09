package com.ssafy.facemeet.navigation

import LoginScreen
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.facemeet.MainViewModel
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.client.clientNavHost
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.navigation.setting.settingNavHost
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.core.util.Animation.NavigationAnimations
import com.ssafy.facemeet.ui.web.SocialProvider
import com.ssafy.facemeet.ui.web.WebLoginScreen

private const val TAG = "MainNavHost"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    isLoggedIn: Boolean,
    mainViewModel: MainViewModel,
) {

    val navController = rememberNavController()
    val bottomNavController = rememberNavController()
//    val startDestination = if (!isLoggedIn) AppRoutes.Start.route else ClientRoutes.MainMenu.route
    val startDestination = AppRoutes.Start.route

    val cameraVM: CameraShotViewModel = hiltViewModel()
    val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        Log.d("AppNavHost", "🎯 pendingNavigation 구독 시작")
        mainViewModel.pendingNavigation.collect { navigation ->
            navigation?.let { (screen, roomId) ->
                Log.d("AppNavHost", "📥 네비게이션 이벤트 수신: $screen, $roomId")
                if (screen == "chatList" && roomId != null) {
                    Log.d("AppNavHost", "🚀 채팅방 이동 실행")
                    navController.navigate(ClientRoutes.Chat.createRoute(roomId))
                    mainViewModel.clearPendingNavigation()
                }
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = NavigationAnimations.defaultEnterTransition(),
        exitTransition = NavigationAnimations.defaultExitTransition(),
        popEnterTransition = NavigationAnimations.defaultEnterTransition(),
        popExitTransition = NavigationAnimations.defaultExitTransition()
    ) {

        Log.d(TAG, "AppNavHost: start")
        // 로그인 화면
        composable(AppRoutes.Start.route) {
            LoginScreen(
                onNavigateToKakaoLogin = {
                    navController.navigate(AppRoutes.WebLogin.createRoute("KAKAO"))
                },
                onNavigateToNaverLogin = {
                    navController.navigate(AppRoutes.WebLogin.createRoute("NAVER"))
                }
            )
        }

        composable(
            route = AppRoutes.WebLogin.route,
            arguments = listOf(navArgument("provider") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerString = backStackEntry.arguments?.getString("provider")
            val provider = SocialProvider.from(providerString)

            Log.d(TAG, "AppNavHost: weblogin/${provider}")

            WebLoginScreen(
                provider = provider,
                onLoginSuccess = { hasInfo, hasFace ->
                    Log.d(TAG, "hasInfo: ${hasInfo}  hasFace : ${hasFace}")

                    if (!hasInfo) {
                        navController.navigate(SettingRoutes.Register.route) {
                            popUpTo(AppRoutes.Start.route) { inclusive = false }
                        }
                    } else if (!hasFace) {
                        navController.navigate(SettingRoutes.CameraStart.route) {
                            popUpTo(AppRoutes.Start.route) { inclusive = true }
                        }
                    } else {
                        Log.d(TAG, "AppNavHost: else")
                        navController.navigate(ClientRoutes.MainMenu.route) {
                            popUpTo(AppRoutes.Start.route) { inclusive = true }
                        }
                    }
                },
                onLoginFailed = {
                    Log.d(TAG, "AppNavHost: Failed")
                    navController.popBackStack(AppRoutes.Start.route, inclusive = false)
                },
                onCancel = {
                    Log.d(TAG, "AppNavHost: onCancel")
                    navController.popBackStack(AppRoutes.Start.route, inclusive = false)
                }
            )
        }

        settingNavHost(navController, cameraVM, analyzeVM)
        clientNavHost(navController, bottomNavController)
    }
}