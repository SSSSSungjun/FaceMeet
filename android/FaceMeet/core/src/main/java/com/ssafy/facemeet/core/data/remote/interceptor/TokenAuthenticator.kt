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

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: Provider<AuthApiService>,
    private val tokenExpirationNotifier: TokenExpirationNotifier,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return try {
            Log.d("TokenAuthenticator", "401 응답으로 인한 authenticate 호출")

            // runBlocking으로 suspend 함수들 호출
            val currentAccessToken = runBlocking {
                tokenManager.getAccessToken()
            } ?: throw Exception("accessToken is null")

            val currentRefreshToken = runBlocking {
                tokenManager.getRefreshToken()
            } ?: throw Exception("refreshToken is null")

            Log.d(
                "TokenAuthenticator",
                "현재 accessToken : $currentAccessToken\n현재 refreshToken : $currentRefreshToken"
            )

            synchronized(this) {
                Log.d("TokenAuthenticator", "synchronized 블록 내부 진입")
                if (currentAccessToken != response.request.header("Authorization")?.removePrefix("Bearer ")) {
                    Log.d("TokenAuthenticator", "토큰이 이미 갱신되었으므로 요청 재시도")
                    return response.request.createRequestWithRenewedToken(currentAccessToken)
                }

                // ✅ 핵심 수정: runBlocking 안에서 runCatching을 사용합니다.
                val newTokenResponseResult = runBlocking {
                    Log.d("TokenAuthenticator", "refresh API 호출 중...")
                    runCatching { // 이 안에서 발생하는 모든 예외를 Result로 감쌉니다.
                        authApiService.get().postRefreshToken(RefreshTokenRequest(currentRefreshToken))
                    }
                }

                // ✅ Result 객체에서 성공 또는 실패를 처리합니다.
                val newTokenResponse = newTokenResponseResult.getOrElse { exception ->
                    Log.e("TokenAuthenticator", "❌ refresh API 호출 중 예외 발생: ${exception.message}", exception)
                    // 예외가 발생했으므로 세션 만료 처리
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null // 예외 발생 시 null 반환
                }

                if (!newTokenResponse.isSuccessful) {
                    Log.e("TokenAuthenticator", "토큰 갱신 실패: HTTP ${newTokenResponse.code()}")
                    if (newTokenResponse.code() == 401 || newTokenResponse.code() == 403) {
                        Log.e("TokenAuthenticator", "❌ 세션 만료 - Refresh token이 만료되었습니다. (HTTP ${newTokenResponse.code()})")
                    } else {
                        Log.e("TokenAuthenticator", "❌ 세션 만료 - 토큰 갱신 중 알 수 없는 HTTP 오류: ${newTokenResponse.code()}")
                    }
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null // 세 번째 null 반환 지점
                }

                val newTokenData = newTokenResponse.body()
                Log.d("TokenAuthenticator", "토큰 갱신 응답 받음: ${newTokenData}") // 응답 body 로그 추가
                Log.d("TokenAuthenticator", "응답 body 파싱 완료")

                if (newTokenData == null) {
                    Log.e("TokenAuthenticator", "❌ 세션 만료 - 응답 body가 null입니다. refresh token 문제로 추정됩니다.")
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null // 네 번째 null 반환 지점
                }

                val newAccessToken = newTokenData.accessToken
                if (newAccessToken == null) {
                    Log.e("TokenAuthenticator", "❌ 세션 만료 - 새 accessToken이 null입니다.")
                    tokenManager.clearTokensSync()
                    runBlocking { tokenExpirationNotifier.notifyTokenExpired() }
                    return null // 다섯 번째 null 반환 지점
                }

                val responseRefreshToken = newTokenData.refreshToken
                val finalRefreshToken = if (responseRefreshToken != null && responseRefreshToken != currentRefreshToken) {
                    Log.d("TokenAuthenticator", "refreshToken이 변경되었습니다: $currentRefreshToken -> $responseRefreshToken")
                    responseRefreshToken
                } else {
                    Log.d("TokenAuthenticator", "refreshToken 유지: $currentRefreshToken")
                    currentRefreshToken
                }

                Log.d("TokenAuthenticator", "토큰 저장 시작")
                runBlocking {
                    tokenManager.saveTokens(newAccessToken, finalRefreshToken)
                }
                Log.d("TokenAuthenticator", "토큰 저장 완료")

                Log.d("TokenAuthenticator", "갱신 accessToken : $newAccessToken\n갱신 refreshToken : $finalRefreshToken")

                response.request.createRequestWithRenewedToken(newAccessToken)

            }
        } catch (e: Exception) {
            Log.d("TokenAuthenticator", "토큰 갱신 실패: ${e.message}")
            null
        }
    }

    private fun Request.createRequestWithRenewedToken(renewedToken: String): Request = newBuilder()
        .header("Authorization", "Bearer $renewedToken")
        .build()
}