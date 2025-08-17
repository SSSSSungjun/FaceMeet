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

// TokenAuthenticator.kt

private const val TAG = "TokenAuthenticator"

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: Provider<AuthApiService>,
    private val tokenExpirationNotifier: TokenExpirationNotifier,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        try {
            Log.d(TAG, "401 응답으로 인한 authenticate 호출")

            if (response.request.header("X-Retry") == "1") {
                Log.w(TAG, "두 번째 401 감지 → 토큰 삭제 & 로그아웃 처리")
                tokenManager.clearTokensSync()
                runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                return null
            }

            if (response.request.url.encodedPath.contains("/auth/refresh")) return null

            val reqToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            val currentAccessToken = runBlocking { tokenManager.getAccessToken() } ?: return null
            val currentRefreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

            // 3) 최신 토큰과 다르면 → 빠른 재시도 (refresh 안 함)
            if (reqToken != null && reqToken != currentAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .header("X-Retry", "1") // ★ 재시도 마커
                    .build()
            }

            synchronized(this) {
                // 이중 체크
                if (currentAccessToken != response.request.header("Authorization")
                        ?.removePrefix("Bearer ")
                ) {
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .header("X-Retry", "1")
                        .build()
                }

                // 4) refresh API 호출
                val newTokenResponseResult = runBlocking {
                    Log.d(TAG, "refresh API 호출 중...")
                    runCatching {
                        authApiService.get()
                            .postRefreshToken(RefreshTokenRequest(currentRefreshToken))
                    }
                }

                val newTokenResponse = newTokenResponseResult.getOrElse { exception ->
                    Log.e(TAG, "❌ refresh API 호출 중 예외: ${exception.message}", exception)
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null
                }

                // 5) refresh 결과가 실패(401/403 포함)면 토큰 삭제 후 null
                if (!newTokenResponse.isSuccessful) {
                    Log.e(TAG, "토큰 갱신 실패: HTTP ${newTokenResponse.code()}")
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null
                }

                val newTokenData = newTokenResponse.body() ?: run {
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null
                }

                val newAccessToken = newTokenData.accessToken ?: run {
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null
                }

              //  val newAccessToken = newTokenData.accessToken
                val responseRefreshToken = newTokenData.refreshToken
                val finalRefreshToken =
                    if (responseRefreshToken != null && responseRefreshToken != currentRefreshToken) {
                        responseRefreshToken
                    } else currentRefreshToken

                // 6) 새 토큰 저장
                runBlocking { tokenManager.saveTokens(newAccessToken, finalRefreshToken) }

                // 7) 새 토큰 + 재시도 마커 추가해서 반환
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .header("X-Retry", "1") // ★ 이게 두 번째 시도 구분 포인트
                    .build()
            }
        } catch (e: Exception) {
            Log.e(TAG, "토큰 갱신 실패: ${e.message}", e)
            return null
        }
    }

}