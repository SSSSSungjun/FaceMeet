package com.ssafy.facemeet.ui.web

import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.ui.SocialLoginProvider
import com.ssafy.facemeet.util.configureKakaoWebView

const val BASE_KAKAO_URL = "http://i13d201.p.ssafy.io/oauth2/authorization/kakao"

private const val TAG = "WebLoginScreen"

@Composable
fun WebLoginScreen(
    type: SocialLoginProvider,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val webViewModel: WebViewModel = hiltViewModel()

    when (type) {
        SocialLoginProvider.KAKAO -> {
            Box(modifier=Modifier.fillMaxSize().systemBarsPadding()){
                AndroidView(
                    factory = {
                        WebView(context).apply {
                            Log.d(TAG, "WebLoginScreen: 호출성공")
                            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                            configureKakaoWebView(
                                onTokenExtracted = { accessToken, refreshToken ->
                                    Log.d(TAG, "로그인 성공")
                                    webViewModel.saveTokens(accessToken, refreshToken)
                                    onBack()
                                },
                                onError = { error ->
                                    Log.e(TAG, "로그인 에러: $error")
                                    onBack()
                                },
                                onCancel = {
                                    Log.d(TAG, "로그인 취소")
                                    onBack()
                                },
                                onDismiss = {
                                    onBack()
                                }
                            )

                            loadUrl(BASE_KAKAO_URL)

                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

        }

        SocialLoginProvider.NAVER -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = " bbbbbb ",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        SocialLoginProvider.NONE -> {
            onBack()
        }

    }
}

