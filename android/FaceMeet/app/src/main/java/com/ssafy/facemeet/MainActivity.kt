package com.ssafy.facemeet

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ssafy.facemeet.navigation.AppNavHost
import com.ssafy.facemeet.service.OfflineNotifyService
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mainViewModel.isLoading.value }

        super.onCreate(savedInstanceState)

        // 오프라인 처리를 위해 필요
        val serviceIntent = Intent(this, OfflineNotifyService::class.java)
        startService(serviceIntent)

        enableEdgeToEdge()
        setContent {
            FacemeetTheme {
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                if (isLoggedIn != null)
                    AppNavHost(isLoggedIn == true)
            }
        }

    }

}

