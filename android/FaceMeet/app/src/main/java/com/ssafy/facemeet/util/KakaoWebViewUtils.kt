package com.ssafy.facemeet.util

import android.net.Uri
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import kotlinx.serialization.json.JsonObject

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
            // 페이지 내용에서 토큰을 찾기 위해 JavaScript 실행
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

        // 1. 직접 JSON 파싱 시도
        val gson = Gson()

        // JSON 객체가 그대로 있는 경우
        try {
            val jsonObject = gson.fromJson(text, JsonObject::class.java)
            val accessToken = jsonObject.get("accessToken")?.toString()
            val refreshToken = jsonObject.get("refreshToken")?.toString()

            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                Log.d(TAG, "직접 JSON 파싱 성공")
                return Pair(accessToken, refreshToken) as Pair<String, String>?
            }
        } catch (e: Exception) {
            Log.d(TAG, "직접 JSON 파싱 실패, 다른 방법 시도")
        }

        // 2. JSON이 텍스트 안에 포함된 경우 추출
        val jsonRegex = """\{[^{}]*"accessToken"[^{}]*"refreshToken"[^{}]*\}""".toRegex()
        val jsonMatch = jsonRegex.find(text)

        if (jsonMatch != null) {
            val jsonString = jsonMatch.value
            Log.d(TAG, "JSON 문자열 추출: $jsonString")

            try {
                val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                val accessToken = jsonObject.get("accessToken")?.toString()
                val refreshToken = jsonObject.get("refreshToken")?.toString()

                if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                    Log.d(TAG, "JSON 추출 파싱 성공")
                    return Pair(accessToken, refreshToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON 파싱 실패", e)
            }
        }

        val accessTokenRegex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
        val refreshTokenRegex = """"refreshToken"\s*:\s*"([^"]+)"""".toRegex()

        val accessMatch = accessTokenRegex.find(text)
        val refreshMatch = refreshTokenRegex.find(text)

        if (accessMatch != null && refreshMatch != null) {
            val accessToken = accessMatch.groupValues[1]
            val refreshToken = refreshMatch.groupValues[1]

            Log.d(TAG, "정규식 파싱 성공")
            return Pair(accessToken, refreshToken)
        }
        Log.w(TAG, "모든 파싱 방법 실패")
        null

    } catch (e: Exception) {
        Log.e(TAG, "토큰 파싱 중 전체 오류", e)
        null
    }
}

