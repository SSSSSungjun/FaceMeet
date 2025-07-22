package com.ssafy.facemeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ssafy.facemeet.core.navigation.Routes
import com.ssafy.facemeet.theme.FacemeetTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            FacemeetTheme {
                FaceMeetApp(Routes.Main.route) // 임시. 루트는 추후 정의해야함dd
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FacemeetTheme {
        FaceMeetApp(Routes.Main.route)
    }
}