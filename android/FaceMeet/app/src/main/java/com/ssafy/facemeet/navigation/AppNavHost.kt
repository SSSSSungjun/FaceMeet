package com.ssafy.facemeet.navigation

import LoginScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.client.clientNavHost
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.navigation.setting.settingNavHost
import com.ssafy.facemeet.ui.web.SocialProvider
import com.ssafy.facemeet.ui.web.WebLoginScreen

private const val TAG = "MainNavHost"

@Composable
fun AppNavHost(isLoggedIn: Boolean) {

    val navController = rememberNavController()
    // val startDestination =if(!isLoggedIn) AppRoutes.Start.route else ModuleEntryRoute.ClientMainMenu.route
    val startDestination = AppRoutes.Start.route

    NavHost(
        navController = navController,
        startDestination = startDestination, // 이 값 말고는 예외 없음
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
                onLoginSuccess = { isNewUser ->
                    Log.d(TAG, "AppNavHost: ${isNewUser}")
                    if (isNewUser) {
                        navController.navigate(SettingRoutes.Register.route) {
                            popUpTo(AppRoutes.Start.route) { inclusive = false }
                        }
                    } else {
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

        settingNavHost(navController)
        clientNavHost(navController)
    }
}