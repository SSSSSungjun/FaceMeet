package com.ssafy.facemeet

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { mainViewModel.isLoading.value }
        }
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d("FCM_EXTRA", "$key: key , $value : value")
            }
        }

        handleNotificationIntent(intent)

        val serviceIntent = Intent(this, OfflineNotifyService::class.java)
        startService(serviceIntent)

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
//                LaunchedEffect(Unit) { this@MainActivity.navController = nc } // NavHost 생성 완료 후 한 번 의도 전달(선택)
                LaunchedEffect(intent) {
                    handleNotificationIntent(intent)
                    intent.removeExtra("deep_link")
                    intent.removeExtra("settingId")
                }

                AppNavHost(
                    isLoggedIn == true,
                    mainViewModel,
                    nc,
                    startIntent.getBooleanExtra("hasInitialDeepLink", false)
                )


            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    // MainActivity
    private fun handleNotificationIntent(intent: Intent) {
        lifecycleScope.launch {
            Log.d("MainActivity", "handleNotificationIntent: ${intent.getStringExtra("deep_link")}")
            Log.d(TAG, "*handleNotificationIntent 진입")
            var deepLink = intent.getStringExtra("deep_link")
            if (deepLink == null) {
                deepLink = intent.getStringExtra("type")
            }
            Log.d(TAG, "deepLink: $deepLink")
            when (deepLink) {
                "SCHEDULED_EVENT" -> {
                    Log.d(TAG, "handleNotificationIntent: ticket_event")
                    val id = intent.getLongExtra("settingId", -1L)
                    Log.d(TAG, "handleNotificationIntent: $id")
                    if (id > 0) {
                        Log.d(TAG, "id > 0")
                        mainViewModel.setPendingTicketEvent(id)
                    }
                }

                "CHAT" -> {
                    val roomId = intent.getStringExtra("roomId")?.toLong()
                    Log.d(TAG, "deepLink: $roomId")
                    mainViewModel.setPendingChat(roomId ?: 0L)
                }

                "PRE_MESSAGE" -> mainViewModel.setPendingNotificationCenter()
            }
        }

    }

}


//// MainActivity.kt (또는 적절한 파일)
//private fun mapDeepLinkToRoute(data: android.net.Uri?): String? {
//    data ?: return null
//    // facemeet://app/<path>...
//    val segments = data.pathSegments
//    if (segments.isEmpty()) return null
//
//    return when (segments[0]) {
//        "main" -> com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route
//        "notification" -> com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route
//        "chat" -> {
//            val roomId = segments.getOrNull(1)?.toLongOrNull() ?: return null
//            com.ssafy.facemeet.client.navigation.client.ClientRoutes.Chat.createRoute(roomId)
//        }
//
//        "ticket_event" -> {
//            val settingId = segments.getOrNull(1)?.toLongOrNull() ?: return null
//            com.ssafy.facemeet.client.navigation.client.ClientRoutes.TicketEvent.createRoute(
//                settingId
//            )
//        }
//
//        "partner_profile" -> {
//            val partnerId = segments.getOrNull(1)?.toLongOrNull() ?: return null
//            val roomId = segments.getOrNull(2)?.toLongOrNull() ?: return null
//            com.ssafy.facemeet.client.navigation.client.ClientRoutes.PartnerProfile.routeWithArgs(
//                partnerId,
//                roomId
//            )
//        }
//
//        else -> null
//    }
//}


//// 어디든 공용 위치 (예: AppNavHost 파일 하단)
//private fun androidx.navigation.NavController.goToWithMainThenNotification(
//    targetRoute: String
//) {
//    val isOnMain = currentDestination
//        ?.hierarchy
//        ?.any { it.route == com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route } == true
//
//    // 1) 메인을 베이스로
//    if (!isOnMain) {
//        navigate(com.ssafy.facemeet.client.navigation.client.ClientRoutes.MainMenu.route) {
//            popUpTo(com.ssafy.facemeet.navigation.AppRoutes.Start.route) { inclusive = true }
//            launchSingleTop = true
//            restoreState = false
//        }
//    }
//
//    // 2) 알림목록 (타깃이 알림목록 자체가 아닐 때만)
//    if (targetRoute != com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route) {
//        val alreadyOnNoti = currentDestination
//            ?.hierarchy
//            ?.any { it.route == com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route } == true
//        if (!alreadyOnNoti) {
//            navigate(com.ssafy.facemeet.client.navigation.client.ClientRoutes.Notification.route) {
//                launchSingleTop = true
//            }
//        }
//    }
//
//    // 3) 타깃 화면
//    navigate(targetRoute) {
//        launchSingleTop = true
//    }
//}
