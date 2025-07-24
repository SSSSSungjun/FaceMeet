package com.ssafy.facemeet.util

import android.util.Log

private const val TAG = "KaKaoLoginWebView"

fun parseTokensFromText(text: String): Pair<String, String>? {
    return try {
        val accessTokenRegex = """accessToken\s*:\s*([^,}]+)""".toRegex()
        val refreshTokenRegex = """refreshToken\s*:\s*([^,}]+)""".toRegex()

        val accessToken = accessTokenRegex.find(text)?.groupValues?.get(1)?.trim()
        val refreshToken = refreshTokenRegex.find(text)?.groupValues?.get(1)?.trim()

        if (accessToken != null && refreshToken != null) {
            accessToken to refreshToken
        } else null
    } catch (e: Exception) {
        Log.e(TAG, "토큰 파싱 에러", e)
        null
    }
}
