package com.ssafy.facemeet.ui.web

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

const val BASE_KAKAO_URL = "https://i13d201.p.ssafy.io/oauth2/authorization/kakao"
const val BASE_NAVER_URL = "https://i13d201.p.ssafy.io/oauth2/authorization/naver"

private const val TAG = "WebLoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLoginScreen(
    provider: SocialProvider,
    onLoginSuccess: (isNew: Boolean) -> Unit,
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

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = {
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        configureWebView(
                            onTokenExtracted = { accessToken, refreshToken, isNew ->
                                Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                                viewModel.saveToken(refreshToken, accessToken)
                                onLoginSuccess(isNew)
                            }, onError = { errorMessage ->
                                Toast.makeText(context, "로그인 에러: $errorMessage", Toast.LENGTH_SHORT).show()
                                onLoginFailed()
                            }, onCancel = {
                                Toast.makeText(context, "로그인 취소", Toast.LENGTH_SHORT).show()
                                onCancel()
                            }, onDismiss = {

                            })
                        loadUrl(url)
                    }
                }, modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .width(50.dp)
                    .height(70.dp)
                    .background(Color.White)
                    .clickable { onCancel() }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun WebLoginScreenPreview() {
    WebLoginScreen(
        provider = SocialProvider.KAKAO,
        onLoginSuccess = { },
        onLoginFailed = { },
        onCancel = { })
}