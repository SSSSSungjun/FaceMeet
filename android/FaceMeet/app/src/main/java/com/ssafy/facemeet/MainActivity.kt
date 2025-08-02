package com.ssafy.facemeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ssafy.facemeet.navigation.AppNavHost
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mainViewModel.isLoading.value }

        super.onCreate(savedInstanceState)
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

