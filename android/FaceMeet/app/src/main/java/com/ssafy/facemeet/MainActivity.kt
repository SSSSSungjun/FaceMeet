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
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mainViewModel.isLoading.value }

        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        val serviceIntent = Intent(this, OfflineNotifyService::class.java)
        startService(serviceIntent)

        lifecycleScope.launch {
            tokenExpirationNotifier.tokenExpiredEvent.collect {
                Toast.makeText(this@MainActivity, "세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG)
                    .show()

                // 로그아웃
                mainViewModel.logout()

            }
        }

        enableEdgeToEdge()
        setContent {
            FacemeetTheme {
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                if (isLoggedIn != null) {
                    AppNavHost(isLoggedIn == true, mainViewModel)

                    // NavHost 생성 완료 후 한 번 의도 전달(선택)
                    LaunchedEffect(Unit) {
                        handleNotificationIntent(intent)
                        intent.removeExtra("deep_link")
                        intent.removeExtra("settingId")
                    }
                }
            }


        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    // MainActivity
    private fun handleNotificationIntent(intent: Intent) {
        Log.d(TAG, "*handleNotificationIntent 진입")
        when (intent.getStringExtra("deep_link")) {
            "ticket_event" -> {
                Log.d(TAG, "handleNotificationIntent: ticket_event")
                val id = intent.getLongExtra("settingId", -1L)
                Log.d(TAG, "handleNotificationIntent: ${id}")
                if (id > 0) {
                    Log.d(TAG, "id > 0")
                    mainViewModel.setPendingTicketEvent(id)
                }
            }

            "chat" -> {
                val roomId = intent.getLongExtra("roomId", -1L)
                if (roomId > 0) mainViewModel.setPendingChat(roomId)
            }

            "notification_center" -> mainViewModel.setPendingNotificationCenter()
        }
    }


}

