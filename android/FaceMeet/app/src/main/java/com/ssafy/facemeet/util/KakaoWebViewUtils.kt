package com.ssafy.facemeet.util

import android.net.Uri
import android.util.Log
import android.webkit.WebView

private const val TAG = "KakaoWebViewUtils"

internal fun isCancelUrl(url: String): Boolean {
    return url.contains("cancel") || url.contains("error") || url.contains("denied")
}

internal fun handleTokenExtraction(
    url: String,
    onTokenExtracted: (String, String) -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    try {
        val uri = Uri.parse(url)
        val accessToken = uri.getQueryParameter("accessToken")
        val refreshToken = uri.getQueryParameter("refreshToken")

        if (accessToken != null && refreshToken != null) {
            onTokenExtracted(accessToken, refreshToken)
            onDismiss()
            return
        }

        val tokens = parseTokensFromText(url)
        if (tokens != null) {
            onTokenExtracted(tokens.first, tokens.second)
            onDismiss()
        } else {
            onError("토큰을 찾을 수 없습니다")
        }

    } catch (e: Exception) {
        Log.e(TAG, "Token extraction error", e)
        onError("토큰 추출 중 오류 발생: ${e.message}")
    }
}

internal fun checkForTokens(
    view: WebView?,
    url: String?,
    onTokenExtracted: (String, String) -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    url?.let { currentUrl ->
        when {
            isCancelUrl(currentUrl) -> {
                onCancel()
                onDismiss()
            }

            currentUrl.contains("accessToken") && currentUrl.contains("refreshToken") -> {
                handleTokenExtraction(currentUrl, onTokenExtracted, onError, onDismiss)
            }

            else -> {
                view?.evaluateJavascript("document.body.innerText") { result ->
                    if (result != null && result != "null" && result.contains("accessToken")) {
                        val cleanText = result.replace("\"", "").replace("\\", "")
                        val tokens = parseTokensFromText(cleanText)
                        if (tokens != null) {
                            onTokenExtracted(tokens.first, tokens.second)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

private fun parseTokensFromText(text: String): Pair<String, String>? {
    return try {
        Log.d(TAG, "파싱할 텍스트: $text")

        val accessTokenRegex = """accessToken\s*:\s*([^,}]+)""".toRegex()
        val refreshTokenRegex = """refreshToken\s*:\s*([^,}]+)""".toRegex()

        val accessMatch = accessTokenRegex.find(text)
        val refreshMatch = refreshTokenRegex.find(text)

        val accessToken = accessMatch?.groupValues?.get(1)?.trim('"', ' ')
        val refreshToken = refreshMatch?.groupValues?.get(1)?.trim('"', ' ')

        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            Log.d(TAG, "정규식으로 토큰 추출 성공")
            return Pair(accessToken, refreshToken)
        }

        null
    } catch (e: Exception) {
        Log.e(TAG, "토큰 파싱 중 전체 오류", e)
        null
    }
}


