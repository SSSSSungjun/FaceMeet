package com.ssafy.facemeet.ui.web

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
    val url = when (provider) {
        SocialProvider.NAVER -> BASE_NAVER_URL
        SocialProvider.KAKAO -> BASE_KAKAO_URL
        else -> ""
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),// topBar 전체 높이
    ) {
        AndroidView(
            factory = {
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    configureWebView(
                        onTokenExtracted = { accessToken, refreshToken, hasInfo, hasFace ->
                            Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT)
                            viewModel.saveToken(refreshToken, accessToken)
                            onLoginSuccess(hasInfo, hasFace)
                        },
                        onError = { error ->
                            Toast.makeText(context, "로그인 에러", Toast.LENGTH_SHORT)
                            onLoginFailed()
                        },
                        onCancel = {
                            Toast.makeText(context, "로그인 취소", Toast.LENGTH_SHORT)
                            onCancel()
                        },
                        onDismiss = {}
                    )
                    loadUrl(url)
                }
            },

        )


    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun WebLoginScreenPreview() {
//    WebLoginScreen(
//        provider = SocialProvider.KAKAO,
//        onLoginSuccess = {  },
//        onLoginFailed = { },
//        onCancel = { })
//}