package com.ssafy.facemeet.navigation

import LoginScreen
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.facemeet.MainViewModel
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.client.clientNavHost
import com.ssafy.facemeet.client.navigation.setting.RegisterMode
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.navigation.setting.settingNavHost
import com.ssafy.facemeet.client.ui.camera.CameraShotViewModel
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.core.util.Animation.NavigationAnimations
import com.ssafy.facemeet.navigation.gate.LoginGateScreen
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
//    val startDestination = if (!isLoggedIn) {
//        AppRoutes.Start.route
//    } else {
//        AppRoutes.LoginGate.route
//    }
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
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    navController.printBackStack()

                    navController.navigate(AppRoutes.LoginGate.route) {
                        launchSingleTop = true         // LoginGate 중복 방지
                        // popUpTo 하지 않음 → Start가 백스택에 남아 바닥으로 깔림
                    }
                }
            }

            if (isLoggedIn) {
                // 스플래시/플레이스홀더 화면 (로고 넣어도 됨)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF4F3ED))
                )
            } else {
                // 로그인 필요 시에만 진짜 LoginScreen 표시
                LoginScreen(
                    onNavigateToKakaoLogin = {
                        navController.navigate(AppRoutes.WebLogin.createRoute("KAKAO"))
                    },
                    onNavigateToNaverLogin = {
                        navController.navigate(AppRoutes.WebLogin.createRoute("NAVER"))
                    }
                )
            }
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
                    navController.printBackStack()
                    navController.navigate(AppRoutes.LoginGate.route) {
                        popUpTo(AppRoutes.Start.route) {
                            inclusive = false
                        } // 로그인 화면 스택 유지/제거는 취향대로
                        launchSingleTop = true
                    }

//                    if (!hasInfo) {
//                        Log.d(TAG, "AppNavHost: hasInfo false")
//                        navController.navigate("${SettingRoutes.Register.route}?mode=${RegisterMode.REGISTER.name}")
//                    } else if (!hasFace) {
//                        navController.navigate(SettingRoutes.CameraStart.route) {
//                            popUpTo(AppRoutes.Start.route) { inclusive = true }
//                        }
//                    } else {
//                        Log.d(TAG, "AppNavHost: else")
//                        navController.navigate(ClientRoutes.MainMenu.route) {
//                            popUpTo(AppRoutes.Start.route) { inclusive = true }
//                        }
//                    }
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


        composable(AppRoutes.LoginGate.route) {
            LoginGateScreen(
                onToRegister = {
                    navController.navigate("${SettingRoutes.Register.route}?mode=${RegisterMode.REGISTER.name}") {
                        popUpTo(AppRoutes.Start.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onToCamera = {
                    navController.navigate(SettingRoutes.CameraStart.route) {
                        popUpTo(AppRoutes.Start.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onToMain = {
                    navController.navigate(ClientRoutes.MainMenu.route) {
                        popUpTo(AppRoutes.Start.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }



        settingNavHost(navController, cameraVM, analyzeVM)
        clientNavHost(navController, bottomNavController)
    }
}


@SuppressLint("RestrictedApi")
fun NavController.printBackStack(tag: String = "NavStack") {
    val stack = this.currentBackStack.value.map { entry ->
        entry.destination.route ?: entry.destination.displayName
    }
    Log.d(tag, "Back stack: $stack (current=${currentDestination?.route})")
}
