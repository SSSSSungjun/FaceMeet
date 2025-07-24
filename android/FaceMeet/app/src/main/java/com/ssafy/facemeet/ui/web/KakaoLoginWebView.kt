package com.ssafy.facemeet.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

private const val TAG = "KaKaoLoginWebView"
private const val BASE_DOMAIN = "http://i13d201.p.ssafy.io/"

fun WebView.configureKakaoWebView(
    onTokenExtracted: (accessToken: String, refreshToken: String) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    settings.builtInZoomControls = false
    settings.displayZoomControls = false
    setInitialScale(1)

    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = true
    setBackgroundColor(Color.WHITE)

    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36"

    webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "Page started: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(TAG, "Page finished: $url")

            if (url == null) return
            if (url==BASE_DOMAIN) {
                onDismiss()
                return
            }
            if (isCancelUrl(url)) {
                Log.d(TAG, " WebView close")
                onCancel()
                onDismiss()
                return
            }

            if (url.contains("success") || url.contains("callback") || url.contains("login")) {
                evaluateJavascript(
                    """
                    (function() {
                        var bodyText = document.body.innerText || document.body.textContent || '';
                        return bodyText;
                    })();
                    """.trimIndent()
                ) { result ->
                    val cleanResult = result.replace("\"", "").replace("\\", "")
                    try {
                        if (cleanResult.contains("accessToken") && cleanResult.contains("refreshToken")) {
                            val tokens = parseTokensFromText(cleanResult)
                            if (tokens != null) {
                                onTokenExtracted(tokens.first, tokens.second)
                                onDismiss()
                            } else {

                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "token error", e)
                        onError("token exception: ${e.message}")
                    }
                }
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.e(TAG, "error: ${error?.description}")

            if (error?.errorCode == ERROR_HOST_LOOKUP ||
                error?.errorCode == ERROR_CONNECT ||
                error?.errorCode == ERROR_TIMEOUT) {
                onError("ERROR : ${error.description}")
            }
        }
    }
}

private fun isCancelUrl(url: String): Boolean {
    return url.contains("cancel", ignoreCase = true) ||
            url.contains("deny", ignoreCase = true) ||
            url.contains("error", ignoreCase = true) ||
            url.contains("denied", ignoreCase = true) ||
            url.contains("oauth/cancel", ignoreCase = true) ||
            url.contains("auth/cancel", ignoreCase = true) ||
            (url.contains("error") && url.contains("access_denied"))
}