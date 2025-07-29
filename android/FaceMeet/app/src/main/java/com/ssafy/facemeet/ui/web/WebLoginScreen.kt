package com.ssafy.facemeet.ui.web

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

const val BASE_KAKAO_URL = "http://i13d201.p.ssafy.io/oauth2/authorization/kakao"
const val BASE_NAVER_URL = "http://i13d201.p.ssafy.io/oauth2/authorization/naver"

private const val TAG = "WebLoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLoginScreen(
    provider: SocialProvider,
    onLoginSuccess: (isNew: Boolean) -> Unit,
    onLoginFailed: () -> Unit,
    onCancel: () -> Unit,
    viewModel: WebLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (provider) {
                            SocialProvider.KAKAO -> "카카오 로그인"
                            SocialProvider.NAVER -> "네이버 로그인"
                            else -> ""
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (provider) {
            SocialProvider.KAKAO -> {
                AndroidView(
                    factory = {
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            configureKakaoWebView(
                                onTokenExtracted = { accessToken, refreshToken, isNew ->
                                    Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT)

                                    viewModel.saveToken(refreshToken, accessToken)
                                    onLoginSuccess(isNew)
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
                            loadUrl(BASE_KAKAO_URL)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            SocialProvider.NAVER -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
//                    AndroidView(
//                        factory = {
//                            WebView(context).apply {
//                                Log.d(TAG, "네이버 WebView 초기화")
//                                // 네이버 WebView 설정 (나중에 구현)
//                                configureNaverWebView(
//                                    onTokenExtracted = { accessToken, refreshToken ->
//                                        Log.d(TAG, "네이버 로그인 성공")
//                                        webViewModel.saveTokens(accessToken, refreshToken)
//                                        onLoginSuccess()
//                                    },
//                                    onError = { error ->
//                                        Log.e(TAG, "네이버 로그인 에러: $error")
//                                        onLoginFailed()
//                                    },
//                                    onCancel = {
//                                        Log.d(TAG, "네이버 로그인 취소")
//                                        onBack()
//                                    },
//                                    onDismiss = {
//                                        // WebView 종료시 처리
//                                    }
//                                )
//                                loadUrl(BASE_NAVER_URL)
//                            }
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
                }
            }

            SocialProvider.NONE -> TODO()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WebLoginScreenPreview() {
    WebLoginScreen(SocialProvider.KAKAO, {}, {}, {})
}