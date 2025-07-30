package com.ssafy.facemeet.navigation


import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.facemeet.client.navigation.client.clientNavHost
import com.ssafy.facemeet.client.navigation.setting.settingNavHost
import com.ssafy.facemeet.core.util.constant.ModuleEntryRoute
import com.ssafy.facemeet.ui.web.SocialProvider
import com.ssafy.facemeet.ui.web.WebLoginScreen

private const val TAG = "MainNavHost"

@Composable
fun AppNavHost(isLoggedIn: Boolean) {

    val navController = rememberNavController()
   // val startDestination =if(!isLoggedIn) AppRoutes.Start.route else ModuleEntryRoute.ClientMainMenu.route
    val  startDestination = AppRoutes.Start.route

    NavHost(
        navController = navController,
        startDestination = startDestination, // 이 값 말고는 예외 없음
    ) {
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

                WebLoginScreen(
                    provider = provider,
                    onLoginSuccess = { isNewUser ->
                        if (isNewUser) {
                            navController.navigate(ModuleEntryRoute.Register.route) {
                                popUpTo(AppRoutes.Start.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        } else {
                        navController.navigate(ModuleEntryRoute.ClientMainMenu.route) {
                            popUpTo(AppRoutes.Start.route) { inclusive = true }
                        }
                    }
                },
                onLoginFailed = {
                    navController.popBackStack(AppRoutes.Start.route, inclusive = false)
                },
                onCancel = {
                    navController.popBackStack(AppRoutes.Start.route, inclusive = false)
                }
            )
        }

        settingNavHost(navController)
        clientNavHost(navController)
    }
}