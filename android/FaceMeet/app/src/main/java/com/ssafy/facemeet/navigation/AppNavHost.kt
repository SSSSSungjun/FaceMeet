package com.ssafy.facemeet.navigation

import LoginScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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
    navController: NavHostController,
    hasInitialDeepLink: Boolean,                     // ✅ 추가
) {
    val bottomNavController = rememberNavController()
    val cameraVM: CameraShotViewModel = hiltViewModel()
    val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(AppRoutes.Start.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Start.route,
        enterTransition = NavigationAnimations.defaultEnterTransition(),
        exitTransition = NavigationAnimations.defaultExitTransition(),
        popEnterTransition = NavigationAnimations.defaultEnterTransition(),
        popExitTransition = NavigationAnimations.defaultExitTransition()
    ) {
        composable(AppRoutes.Start.route) {
            if (isLoggedIn) {
                // ✅ 딥링크 없이 켠 경우에만 기본 분기 수행
                LaunchedEffect(Unit) {
                    if (!hasInitialDeepLink) {
                        navController.navigate(AppRoutes.LoginGate.route) {
                            launchSingleTop = true
                            popUpTo(AppRoutes.Start.route) { inclusive = true }
                        }
                    }
                }
                // (임시 배경; 바로 이동하므로 거의 안 보임)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF4F3ED))
                )
            } else {
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
            WebLoginScreen(
                provider = provider,
                onLoginSuccess = { _, _ ->
                    navController.navigate(AppRoutes.LoginGate.route) {
                        popUpTo(AppRoutes.Start.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onLoginFailed = { navController.popBackStack(AppRoutes.Start.route, false) },
                onCancel = { navController.popBackStack(AppRoutes.Start.route, false) }
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
                },
                onToStart = {
                    navController.navigate(AppRoutes.Start.route) {
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
