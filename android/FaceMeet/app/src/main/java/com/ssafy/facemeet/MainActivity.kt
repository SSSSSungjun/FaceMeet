package com.ssafy.facemeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.navigation.AppNavHost
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen=installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.isAppReady
        }

        mainViewModel.initializeApp()

        setContent {
            val isLoading by mainViewModel.isLoading.collectAsState()
            val isLoggedIn by tokenManager.isLoggedInFlow().collectAsState(initial = false)

            FacemeetTheme {
                if (isLoading) {
                    // 로딩 화면 표시
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    AppNavHost(isLoggedIn)
                }
            }
        }

    }
}

