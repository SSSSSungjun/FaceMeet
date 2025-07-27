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
    onTokenExtracted: (String, String, Boolean) -> Unit, // Triple로 변경
    onError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    try {
        val uri = Uri.parse(url)
        val accessToken = uri.getQueryParameter("accessToken")
        val refreshToken = uri.getQueryParameter("refreshToken")
        val isNewParam = uri.getQueryParameter("isNew")

        if (accessToken != null && refreshToken != null) {
            val isNew = isNewParam?.toBoolean() ?: false
            onTokenExtracted(accessToken, refreshToken, isNew)
            onDismiss()
            return
        }

        val tokens = parseTokensFromText(url)
        if (tokens != null) {
            onTokenExtracted(tokens.first, tokens.second, tokens.third)
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
    onTokenExtracted: (String, String, Boolean) -> Unit, // Triple로 변경
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
                            onTokenExtracted(tokens.first, tokens.second, tokens.third)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

private fun parseTokensFromText(text: String): Triple<String, String, Boolean>? {
    return try {
        Log.d(TAG, "파싱할 텍스트: $text")

        // JSON 형태의 토큰 추출을 위한 정규식
        // JSON 형태의 토큰 추출을 위한 정규식 (더 관대한 패턴)
        val accessTokenRegex = """["']?accessToken["']?\s*:\s*["']?([^"',}\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
        val refreshTokenRegex = """["']?refreshToken["']?\s*:\s*["']?([^"',}\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
        val isNewRegex = """["']?isNew["']?\s*:\s*(true|false)""".toRegex(RegexOption.IGNORE_CASE)


        val accessMatch = accessTokenRegex.find(text)
        val refreshMatch = refreshTokenRegex.find(text)
        val isNewMatch = isNewRegex.find(text)

        val accessToken = accessMatch?.groupValues?.get(1)?.trim()
        val refreshToken = refreshMatch?.groupValues?.get(1)?.trim()
        val isNewStr = isNewMatch?.groupValues?.get(1)?.trim()
        val isNew = isNewStr?.toBoolean() ?: false

        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            Log.d(TAG, "정규식으로 토큰 추출 성공 - accessToken: $accessToken, refreshToken: $refreshToken, isNew: $isNew")
            return Triple(accessToken, refreshToken, isNew)
        }

        Log.w(TAG, "토큰 파싱 실패 - accessToken: $accessToken, refreshToken: $refreshToken, isNew: $isNew")
        null
    } catch (e: Exception) {
        Log.e(TAG, "토큰 파싱 중 전체 오류", e)
        null
    }
}