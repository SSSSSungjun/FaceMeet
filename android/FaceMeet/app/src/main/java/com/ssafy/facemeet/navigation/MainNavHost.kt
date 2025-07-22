package com.ssafy.facemeet.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.registerMainNavigation
import com.ssafy.facemeet.core.navigation.Routes
import com.ssafy.facemeet.ui.LoginScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination, //처음엔 무조건 splash 띄우게
    ) {
        // 로그인 화면
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Routes.Admin.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // feature1 모듈의 메인 화면 등록
        registerMainNavigation(navController)

        // feature2 모듈의 관리자 화면 등록
        //registerAdminNavigation(navController)
    }
}