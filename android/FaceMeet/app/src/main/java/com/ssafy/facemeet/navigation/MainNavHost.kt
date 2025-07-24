package com.ssafy.facemeet.navigation


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.navigation.clientNavigation
import com.ssafy.facemeet.core.navigation.Routes
import com.ssafy.facemeet.ui.LoginScreen

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
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToClient = {
                    Log.d(TAG, "MainNavHost: 여기까지 인식")
                    navController.navigate(Routes.Setting.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    } //임시
                },
                onNavigateToAdmin = {
                    navController.navigate(Routes.AdminMain.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        clientNavigation(navController) // 클라이언트 네비게이션
        //adminNavigation(navController)
    }
}