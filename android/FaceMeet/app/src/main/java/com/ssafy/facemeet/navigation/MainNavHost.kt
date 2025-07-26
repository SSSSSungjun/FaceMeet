package com.ssafy.facemeet.navigation


import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ssafy.facemeet.client.navigation.clientNavigation
import com.ssafy.facemeet.core.navigation.Routes
import com.ssafy.facemeet.ui.web.SocialProvider
import com.ssafy.facemeet.ui.web.WebLoginScreen

private const val TAG = "MainNavHost"

@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // 로그인 화면
        composable(startDestination) {
            when(startDestination){

                Routes.Login.route->{
                    LoginScreen(
                        onNavigateToKakaoLogin = {
                            navController.navigate(AppRoutes.WebLogin.createRoute("KAKAO"))
                        },
                        onNavigateToNaverLogin = {
                            navController.navigate(AppRoutes.WebLogin.createRoute("NAVER"))
                        }
                    )
                }

                Routes.ClientMain.route->{

                }

                Routes.AdminMain.route->{

                }

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
                onLoginSuccess = {
                    navController.navigate(Routes.Setting.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onLoginFailed = {
                    navController.popBackStack(Routes.Login.route, inclusive = false)
                },
                onCancel = {
                    navController.popBackStack(Routes.Login.route, inclusive = false)
                }
            )
        }

        clientNavigation(navController) // 클라이언트 네비게이션
        //adminNavigation(navController)
    }
}