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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            // 로그아웃 → Start로 이동 + 백스택 전부 비우기
            navController.navigate(AppRoutes.Start.route) {
                popUpTo(0) { inclusive = true } // 전체 스택 클리어
                launchSingleTop = true
            }
        }
    }

    val startDestination = AppRoutes.Start.route

    val cameraVM: CameraShotViewModel = hiltViewModel()
    val analyzeVM: FaceAnalyzeViewModel = hiltViewModel()

    val pendingTicket by mainViewModel.pendingTicketEvent.collectAsState()
    val pendingChat by mainViewModel.pendingChat.collectAsState()
    val goNotif by mainViewModel.pendingNotificationCenter.collectAsState()

    // ---- 라우팅 단일화: 로그인되면 여기서만 목적지 결정 ----
    LaunchedEffect(isLoggedIn, pendingTicket, pendingChat, goNotif) {
        if (!isLoggedIn) return@LaunchedEffect

        when {
            pendingTicket != null -> {
                val id = checkNotNull(pendingTicket)
                Log.d(TAG, "AppNavHost: ${id}")
                navController.goToWithMainAsBase(ClientRoutes.TicketEvent.createRoute(id))
                mainViewModel.clearPendingTicketEvent()
            }

            pendingChat != null -> {
                val roomId = checkNotNull(pendingChat)
                navController.goToWithMainAsBase(ClientRoutes.Chat.createRoute(roomId))
                mainViewModel.clearPendingChat()
            }

            goNotif -> {
                navController.goToWithMainAsBase(ClientRoutes.Notification.route)
                mainViewModel.clearPendingNotificationCenter()
            }

            else -> {
                // 기본 진입: LoginGate (메인을 베이스로 깔 필요 X)
                navController.navigate(AppRoutes.LoginGate.route) {
                    launchSingleTop = true
                    popUpTo(AppRoutes.Start.route) { inclusive = true }
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
            if (isLoggedIn) {
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

            Log.d(TAG, "AppNavHost: weblogin/${provider}")

            WebLoginScreen(
                provider = provider,
                onLoginSuccess = { hasInfo, hasFace ->
                    Log.d(TAG, "hasInfo: ${hasInfo}  hasFace : ${hasFace}")
                    navController.printBackStack()
                    navController.navigate(AppRoutes.LoginGate.route) {
                        popUpTo(AppRoutes.Start.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
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


/**
 * 메인을 백스택 바닥에 깔고 그 위에 targetRoute를 올린다.
 * → 뒤로가면 항상 메인으로 복귀.
 */
private fun NavController.goToWithMainAsBase(targetRoute: String) {
    // 이미 Main이 현재면 다시 깔지 않고 바로 타깃으로 가도 OK
    val alreadyOnMain = currentDestination?.route == ClientRoutes.MainMenu.route

    if (!alreadyOnMain) {
        // 1) Start를 지우고 Main을 베이스로
        navigate(ClientRoutes.MainMenu.route) {
            popUpTo(AppRoutes.Start.route) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }

    // 2) 그 위에 타깃(티켓/채팅/알림센터) 올리기
    navigate(targetRoute) {
        launchSingleTop = true
        // 메인을 베이스로 유지해야 하므로 popUpTo는 하지 않음
    }
}


@SuppressLint("RestrictedApi")
fun NavController.printBackStack(tag: String = "NavStack") {
    val stack = this.currentBackStack.value.map { entry ->
        entry.destination.route ?: entry.destination.displayName
    }
    Log.d(tag, "Back stack: $stack (current=${currentDestination?.route})")
}
