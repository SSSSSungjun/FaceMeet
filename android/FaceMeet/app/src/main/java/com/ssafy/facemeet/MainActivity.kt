package com.ssafy.facemeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.core.navigation.Routes
import com.ssafy.facemeet.navigation.MainNavHost
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 자동로그인 체크 실행
        mainViewModel.checkAutoLogin()
        setContent {

            val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            FacemeetTheme {
                when (isLoggedIn) {
                    true -> MainNavHost(navController, Routes.ClientMain.route)
                    false -> MainNavHost(navController, Routes.Login.route)
                    null -> {
                        TODO()
                    }
                }
            }
        }
    }
}

