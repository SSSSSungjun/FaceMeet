package com.ssafy.facemeet.navigation

import LoginScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // isLoggedIn 상태에 따라 NavHost의 시작 지점을 바로 결정
    val startDestination = if (isLoggedIn) {
        AppRoutes.LoginGate.route
    } else {
        AppRoutes.Start.route
    }

    val cameraVM: CameraShotViewModel = hiltViewModel()
    val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

    val pendingTicket by mainViewModel.pendingTicketEvent.collectAsState()
    val pendingChat by mainViewModel.pendingChat.collectAsState()
    val goNotif by mainViewModel.pendingNotificationCenter.collectAsState()

    // 알림 및 딥링크 처리 로직
    LaunchedEffect(isLoggedIn, pendingTicket, pendingChat, goNotif) {
        if (!isLoggedIn) return@LaunchedEffect
        Log.d(TAG, "AppNavHost: 로그인 성공 후 추가 라우팅 확인 ${pendingChat ?:"null"} ")
        when {
            pendingTicket != null -> {
                val id = checkNotNull(pendingTicket)
                navController.goToWithMainAsBase(ClientRoutes.TicketEvent.createRoute(id))
                mainViewModel.clearPendingTicketEvent()
            }

            pendingChat != null -> {
                val roomId = checkNotNull(pendingChat)
                Log.d(TAG, "AppNavHost: pendingChat roomID : ${roomId}")
                navController.goToWithMainAsBase(ClientRoutes.Chat.createRoute(roomId))
                mainViewModel.clearPendingChat()
            }

            goNotif -> {
                navController.goToWithMainAsBase(ClientRoutes.Notification.route)
                mainViewModel.clearPendingNotificationCenter()
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
                onLoginSuccess = { hasInfo, hasFace ->
                    navController.navigate(AppRoutes.LoginGate.route) {
                        popUpTo(AppRoutes.Start.route) { inclusive = false }
                        launchSingleTop = true
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


        composable(
            route=AppRoutes.LoginGate.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
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


/**
 * 메인을 백스택 바닥에 깔고 그 위에 targetRoute를 올린다.
 * → 뒤로가면 항상 메인으로 복귀.
 */
private fun NavController.goToWithMainAsBase(targetRoute: String) {
    val alreadyOnMain = currentDestination?.route == ClientRoutes.MainMenu.route

    if (!alreadyOnMain) {
        navigate(ClientRoutes.MainMenu.route) {
            popUpTo(AppRoutes.Start.route) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }
    navigate(targetRoute) {
//        if (targetRoute.contains("chat", ignoreCase = true)){
//            popUpTo(ClientRoutes.Chat.route) {
//                inclusive = false // MainMenu 라우트는 제거하지 않고 남겨둠
//            }
//        }
        launchSingleTop = true
    }
}