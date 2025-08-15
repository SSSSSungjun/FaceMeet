package com.ssafy.facemeet

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.core.data.remote.interceptor.TokenExpirationNotifier
import com.ssafy.facemeet.navigation.AppNavHost
import com.ssafy.facemeet.service.OfflineNotifyService
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var tokenExpirationNotifier: TokenExpirationNotifier

    private lateinit var navController: NavHostController
    private var pendingDeepLink: Intent? = null     // ← 초기화 전 들어올 수도 있으니 보관

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen().apply {
            setKeepOnScreenCondition { mainViewModel.isLoading.value }
        }
        super.onCreate(savedInstanceState)

        startService(Intent(this, OfflineNotifyService::class.java))
        lifecycleScope.launch {
            tokenExpirationNotifier.tokenExpiredEvent.collect {
                Toast.makeText(this@MainActivity, "세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG)
                    .show()
                mainViewModel.logout()
            }
        }

        enableEdgeToEdge()

        // 🔹 콜드 스타트 인텐트 스냅샷
        val startIntent = intent

        setContent {
            FacemeetTheme {
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                val nc = rememberNavController()
                LaunchedEffect(Unit) { this@MainActivity.navController = nc }

                if (isLoggedIn != null) {
                    // 처음 인텐트에 딥링크 있었는지 플래그 (빈화면 방지용)
                    val hasInitialDeepLink = remember { intent?.data != null }

                    AppNavHost(
                        isLoggedIn = isLoggedIn == true,
                        mainViewModel = mainViewModel,
                        navController = nc,
                        hasInitialDeepLink = hasInitialDeepLink
                    )

                    // ✅ NavHost가 붙은 후 한 프레임 뒤에 “직접 라우팅”
                    LaunchedEffect(nc, isLoggedIn) {
                        withFrameNanos { }
                        val route = mapDeepLinkToRoute(intent?.data)
                        if (route != null) {
                            nc.goToWithMainThenNotification(route)
                        } else if (isLoggedIn == true && !hasInitialDeepLink) {
                            // 딥링크 없이 진입 시 기본 진입(예: LoginGate나 Main)
                            nc.navigate(com.ssafy.facemeet.navigation.AppRoutes.LoginGate.route) {
                                launchSingleTop = true
                                popUpTo(com.ssafy.facemeet.navigation.AppRoutes.Start.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!this::navController.isInitialized) return
        val route = mapDeepLinkToRoute(intent.data) ?: return

        navController.goToWithMainThenNotification(route)
    }

}


// MainActivity.kt (또는 적절한 파일)
private fun mapDeepLinkToRoute(data: android.net.Uri?): String? {
    data ?: return null
    // facemeet://app/<path>...
    val segments = data.pathSegments
    if (segments.isEmpty()) return null

    return when (segments[0]) {
        "main" -> com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route
        "notification" -> com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route
        "chat" -> {
            val roomId = segments.getOrNull(1)?.toLongOrNull() ?: return null
            com.ssafy.facemeet.client.navigation.client.ClientRoutes.Chat.createRoute(roomId)
        }

        "ticket_event" -> {
            val settingId = segments.getOrNull(1)?.toLongOrNull() ?: return null
            com.ssafy.facemeet.client.navigation.client.ClientRoutes.TicketEvent.createRoute(
                settingId
            )
        }

        "partner_profile" -> {
            val partnerId = segments.getOrNull(1)?.toLongOrNull() ?: return null
            val roomId = segments.getOrNull(2)?.toLongOrNull() ?: return null
            com.ssafy.facemeet.client.navigation.client.ClientRoutes.PartnerProfile.routeWithArgs(
                partnerId,
                roomId
            )
        }

        else -> null
    }
}


// 어디든 공용 위치 (예: AppNavHost 파일 하단)
private fun androidx.navigation.NavController.goToWithMainThenNotification(
    targetRoute: String
) {
    val isOnMain = currentDestination
        ?.hierarchy
        ?.any { it.route == com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route } == true

    // 1) 메인을 베이스로
    if (!isOnMain) {
        navigate(com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route) {
            popUpTo(com.ssafy.facemeet.navigation.AppRoutes.Start.route) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }

    // 2) 알림목록 (타깃이 알림목록 자체가 아닐 때만)
    if (targetRoute != com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route) {
        val alreadyOnNoti = currentDestination
            ?.hierarchy
            ?.any { it.route == com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route } == true
        if (!alreadyOnNoti) {
            navigate(com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route) {
                launchSingleTop = true
            }
        }
    }

    // 3) 타깃 화면
    navigate(targetRoute) {
        launchSingleTop = true
    }
}
