package com.ssafy.facemeet.ui.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

const val BASE_KAKAO_URL = "https://i13d201.p.ssafy.io/oauth2/authorization/kakao"
const val BASE_NAVER_URL = "https://i13d201.p.ssafy.io/oauth2/authorization/naver"

private const val TAG = "WebLoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLoginScreen(
    provider: SocialProvider,
    onLoginSuccess: (hasInfo: Boolean, hasFace: Boolean) -> Unit,
    onLoginFailed: () -> Unit,
    onCancel: () -> Unit,
    viewModel: WebLoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // WebView 설정은 여기서 한 번만 해줍니다.
            configureWebView(
                onTokenExtracted = { accessToken, refreshToken, hasInfo, hasFace ->
                    mainHandler.post {
                        Toast.makeText(context, "로그인 성공하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.saveToken(refreshToken, accessToken)
                    onLoginSuccess(hasInfo, hasFace)
                },
                onError = { error ->
                    mainHandler.post {
                        Toast.makeText(context, "로그인 에러", Toast.LENGTH_SHORT).show()
                    }
                    onLoginFailed()
                },
                onCancel = {
                    mainHandler.post {
                        Toast.makeText(context, "로그인 취소", Toast.LENGTH_SHORT).show()
                    }
                    onCancel()
                },
                onDismiss = {}
            )
        }
    }

    var isCacheCleared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        WebViewUtils.clearWebViewData(webView, context)

        val url = when (provider) {
            SocialProvider.NAVER -> BASE_NAVER_URL
            SocialProvider.KAKAO -> BASE_KAKAO_URL
            else -> ""
        }

        webView.loadUrl(url)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Log.d(TAG, "WebLoginScreen: isCacheCleared : $isCacheCleared")
        AndroidView(
            factory = { webView },
            update = {}
        )
    }
}