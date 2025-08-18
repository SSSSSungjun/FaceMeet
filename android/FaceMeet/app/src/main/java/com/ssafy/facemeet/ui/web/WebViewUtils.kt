package com.ssafy.facemeet.ui.web

import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView

private const val TAG = "WebViewUtils"

object WebViewUtils {

    fun isCancelUrl(url: String): Boolean {
        return url.contains("cancel") || url.contains("error") || url.contains("denied")
    }

    fun checkForTokens(
        view: WebView?,
        url: String?,
        onTokenExtracted: (String, String, Boolean, Boolean) -> Unit, // accessToken, refreshToken, hasInfo, hasFace
        onError: (String) -> Unit,
        onDismiss: () -> Unit,
        onCancel: () -> Unit
    ) {
        url?.let { currentUrl ->
            Log.d(TAG, "checkForTokens: $currentUrl")
            when {
                isCancelUrl(currentUrl) -> {
                    onCancel()
                    onDismiss()
                }

                else -> {
                    view?.visibility = View.GONE
                    view?.stopLoading()

                    view?.evaluateJavascript("document.body.innerText") { result ->
                        Log.d(TAG, "checkForTokens: $result")
                        if (result != null && result != "null" && result.contains("accessToken")) {
                            val cleanText = result.replace("\"", "").replace("\\", "")
                            val tokens = parseTokensFromText(cleanText)
                            Log.d(TAG, "checkForTokens: $tokens")
                            if (tokens != null) {
                                onTokenExtracted(
                                    tokens.accessToken,
                                    tokens.refreshToken,
                                    tokens.hasInfo,
                                    tokens.hasFace
                                )
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

    data class TokenResult(
        val accessToken: String,
        val refreshToken: String,
        val hasInfo: Boolean,
        val hasFace: Boolean
    )

    private fun parseTokensFromText(text: String): TokenResult? {
        return try {
            Log.d(TAG, "파싱할 텍스트: $text")

            val accessTokenRegex = """accessToken\s*:\s*([^,}]+)""".toRegex()
            val refreshTokenRegex = """refreshToken\s*:\s*([^,}]+)""".toRegex()
            val hasInfoRegex = """hasInfo\s*:\s*(true|false)""".toRegex()
            val hasFaceRegex = """hasFace\s*:\s*(true|false)""".toRegex()

            val accessToken = accessTokenRegex.find(text)?.groupValues?.get(1)?.trim()
            val refreshToken = refreshTokenRegex.find(text)?.groupValues?.get(1)?.trim()
            val hasInfo = hasInfoRegex.find(text)?.groupValues?.get(1)?.toBoolean() == true
            val hasFace = hasFaceRegex.find(text)?.groupValues?.get(1)?.toBoolean() == true

            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                Log.d(
                    TAG,
                    "파싱 성공 - accessToken: $accessToken, refreshToken: $refreshToken, hasInfo: $hasInfo, hasFace: $hasFace"
                )
                return TokenResult(accessToken, refreshToken, hasInfo, hasFace)
            }

            Log.w(
                TAG,
                "파싱 실패 - accessToken: $accessToken, refreshToken: $refreshToken, hasInfo: $hasInfo, hasFace: $hasFace"
            )
            null
        } catch (e: Exception) {
            Log.e(TAG, "토큰 파싱 중 예외 발생", e)
            null
        }
    }

    suspend fun clearWebViewData(webView: WebView, context: Context) {
        try {
            webView.clearCache(true)
            webView.clearHistory()
            webView.clearFormData()

            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()

            WebStorage.getInstance().deleteAllData()
        } catch (e: Exception) {
            Log.e(TAG, "clearWebViewData: ${e.message}", )
        }
    }


}