package com.ssafy.facemeet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssafy.facemeet.navigation.AppNavHost
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()
            Log.d(TAG, "onCreate 토큰: ${isLoggedIn}")
            FacemeetTheme {
                AppNavHost(isLoggedIn)
            }
        }



    }
}

