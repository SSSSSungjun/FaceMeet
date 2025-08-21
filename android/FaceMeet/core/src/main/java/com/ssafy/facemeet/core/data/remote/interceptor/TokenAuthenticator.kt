package com.ssafy.facemeet.core.data.remote.interceptor

import android.util.Log
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.remote.api.AuthApiService
import com.ssafy.facemeet.core.data.remote.dto.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val TAG = "TokenAuthenticator"

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: Provider<AuthApiService>,
    private val tokenExpirationNotifier: TokenExpirationNotifier,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        try {
            Log.d(TAG, "401 응답으로 인해 인증자 호출됨")

            if (response.request.header("X-Retry") == "1") {
                Log.w(TAG, "두 번째 401 감지. 토큰 삭제 및 로그아웃 처리 시작.")
                runBlocking {
                    tokenManager.handleTokenRefreshResult(false)
                    tokenExpirationNotifier.notifyTokenExpired()
                }
                return null
            }

            if (response.request.url.encodedPath.contains("/auth/refresh")) return null

            val reqToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            val currentAccessToken = runBlocking { tokenManager.getAccessToken() } ?: return null
            val currentRefreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

            if (reqToken != null && reqToken != currentAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .header("X-Retry", "1")
                    .build()
            }

            synchronized(this) {
                if (currentAccessToken != response.request.header("Authorization")
                        ?.removePrefix("Bearer ")
                ) {
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .header("X-Retry", "1")
                        .build()
                }

                val newTokenResponseResult = runBlocking {
                    Log.d(TAG, "토큰 갱신 API 호출 중.")
                    runCatching {
                        authApiService.get()
                            .postRefreshToken(RefreshTokenRequest(currentRefreshToken))
                    }
                }

                val newTokenResponse = newTokenResponseResult.getOrElse { exception ->
                    Log.e(TAG, "토큰 갱신 API 호출 중 예외 발생: ${exception.message}", exception)
                    runBlocking {
                        tokenManager.handleTokenRefreshResult(false)
                        tokenExpirationNotifier.notifyTokenExpired()
                    }
                    return null
                }

                if (!newTokenResponse.isSuccessful) {
                    Log.e(TAG, "토큰 갱신 실패: HTTP ${newTokenResponse.code()}")
                    runBlocking {
                        tokenManager.handleTokenRefreshResult(false)
                        tokenExpirationNotifier.notifyTokenExpired()
                    }
                    return null
                }

                val newTokenData = newTokenResponse.body() ?: run {
                    runBlocking {
                        tokenManager.handleTokenRefreshResult(false)
                        tokenExpirationNotifier.notifyTokenExpired()
                    }
                    return null
                }

                val newAccessToken = newTokenData.accessToken ?: run {
                    runBlocking {
                        tokenManager.handleTokenRefreshResult(false)
                        tokenExpirationNotifier.notifyTokenExpired()
                    }
                    return null
                }

                val responseRefreshToken = newTokenData.refreshToken
                val finalRefreshToken =
                    if (responseRefreshToken != null && responseRefreshToken != currentRefreshToken) {
                        responseRefreshToken
                    } else currentRefreshToken

                runBlocking {
                    tokenManager.handleTokenRefreshResult(
                        true,
                        newAccessToken,
                        finalRefreshToken
                    )
                }

                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .header("X-Retry", "1")
                    .build()
            }
        } catch (e: Exception) {
            Log.e(TAG, "인증자 처리 중 오류 발생: ${e.message}", e)
            return null
        }
    }
}