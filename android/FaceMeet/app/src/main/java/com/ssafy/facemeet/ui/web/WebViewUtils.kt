package com.ssafy.facemeet.ui.web

import android.util.Log
import android.view.View
import android.webkit.WebView

private const val TAG = "WebViewUtils"

object WebViewUtils {

    fun isCancelUrl(url: String): Boolean {
        return url.contains("cancel") || url.contains("error") || url.contains("denied")
    }

    fun checkForTokens(
        view: WebView?,
        url: String?,
        onTokenExtracted: (String, String, Boolean) -> Unit,
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
                else -> {
                    view?.visibility = View.GONE
                    view?.stopLoading()

                    view?.evaluateJavascript("document.body.innerText") { result ->
                        if (result != null && result != "null" && result.contains("accessToken")) {

                            val cleanText = result.replace("\"", "").replace("\\", "")
                            val tokens = parseTokensFromText(cleanText)
                            if (tokens != null) {
                                onTokenExtracted(tokens.first, tokens.second, tokens.third)
                                onDismiss()
                            } else {
                                onError("토큰 파싱에 실패했습니다.")
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

            val accessTokenRegex = """["']?accessToken["']?\s*:\s*["']?([^"',}\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
            val refreshTokenRegex = """["']?refreshToken["']?\s*:\s*["']?([^"',}\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
            val isNewRegex = """["']?isNew["']?\s*:\s*(true|false)""".toRegex(RegexOption.IGNORE_CASE)

            val accessMatch = accessTokenRegex.find(text)
            val refreshMatch = refreshTokenRegex.find(text)
            val isNewMatch = isNewRegex.find(text)

            val accessToken = accessMatch?.groupValues?.get(1)?.trim()
            val refreshToken = refreshMatch?.groupValues?.get(1)?.trim()
            val isNewStr = isNewMatch?.groupValues?.get(1)?.trim()
            val isNew = isNewStr?.toBoolean() == true

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


}