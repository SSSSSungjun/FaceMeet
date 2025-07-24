package com.ssafy.facemeet.util

import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log

private const val TAG = "KaKaoLoginWebView"
internal const val BASE_DOMAIN = "http://i13d201.p.ssafy.io/"

fun WebView.configureKakaoWebView(
    onTokenExtracted: (accessToken: String, refreshToken: String) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = false
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_NO_CACHE
        setSupportMultipleWindows(false)
        javaScriptCanOpenWindowsAutomatically = false
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        userAgentString =  WebSettings.getDefaultUserAgent(context)
    }

    setInitialScale(1)
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = true
    setBackgroundColor(Color.WHITE)

    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            Log.d(TAG, "shouldOverrideUrlLoading: $url")
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "Page started: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(TAG, "Page finished: $url")

            url?.let { currentUrl ->
                if (currentUrl.startsWith(BASE_DOMAIN)) {
                    Log.d(TAG, "베이스 도메인 도달")
                    checkForTokens(view, currentUrl, onTokenExtracted, onError, onDismiss,onCancel)
                }
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.e(TAG, "WebView error: ${error?.description}")
            onError("네트워크 오류: ${error?.description}")
        }
    }
}