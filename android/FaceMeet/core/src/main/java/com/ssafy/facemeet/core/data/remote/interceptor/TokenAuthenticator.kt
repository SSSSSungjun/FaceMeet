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

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: Provider<AuthApiService>,
    private val tokenExpirationNotifier: TokenExpirationNotifier
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return try {
            Log.d("TokenAuthenticator", "401 응답으로 인한 authenticate 호출")

            val currentAccessToken = tokenManager.getAccessTokenSync()
                ?: throw Exception("accessToken is null")
            val currentRefreshToken = tokenManager.getRefreshTokenSync()
                ?: throw Exception("refreshToken is null")

            Log.d("TokenAuthenticator", "현재 accessToken : $currentAccessToken\n현재 refreshToken : $currentRefreshToken")

            synchronized(this) {
                if (currentAccessToken != response.request.header("Authorization")?.removePrefix("Bearer ")) {
                    Log.d("TokenAuthenticator", "토큰이 이미 갱신되었으므로 요청 재시도")
                    return response.request.createRequestWithRenewedToken(currentAccessToken)
                }

                try {
                    Log.d("TokenAuthenticator", "토큰 갱신 시작")

                    val newTokenResponse = runBlocking {
                        Log.d("TokenAuthenticator", "refresh API 호출 중...")
                        authApiService.get().postRefreshToken(RefreshTokenRequest(refreshToken = currentRefreshToken))
                    }

                    Log.d("TokenAuthenticator", "토큰 갱신 응답 받음: ${newTokenResponse.body()}")

                    if (!newTokenResponse.isSuccessful) {
                        Log.e("TokenAuthenticator", "토큰 갱신 실패: ${newTokenResponse.code()}")

                        // refresh token 만료 상황 처리
                        if (newTokenResponse.code() == 401 || newTokenResponse.code() == 403) {
                            Log.e("TokenAuthenticator", "Refresh token이 만료되었습니다")
                            tokenManager.clearTokensSync()
                            runBlocking {
                                tokenExpirationNotifier.notifyTokenExpired()
                            }
                            return null  // 여기서 바로 null 반환
                        }

                        // 다른 오류의 경우 예외로 처리 (재시도 가능한 오류)
                        throw Exception("토큰 갱신 실패: ${newTokenResponse.code()}")
                    }

                    val newTokenData = newTokenResponse.body()
                    Log.d("TokenAuthenticator", "응답 body 파싱 완료")

                    if (newTokenData == null) {
                        Log.e("TokenAuthenticator", "응답 body가 null")
                        // body가 null인 경우도 refresh token 문제일 가능성
                        Log.e("TokenAuthenticator", "Response body가 null - refresh token 문제로 추정")
                        tokenManager.clearTokensSync()
                        runBlocking {
                            tokenExpirationNotifier.notifyTokenExpired()
                        }
                        return null
                    }


                    val newAccessToken = newTokenData.accessToken
                    if (newAccessToken == null) {
                        Log.e("TokenAuthenticator", "새 accessToken이 null")
                        throw Exception("새 accessToken이 null")
                    }

                    // 기존 refresh랑 응답 refresh가 다르면 저장 refresh를 새로운걸로 변경
                    val responseRefreshToken = newTokenData.refreshToken
                    val finalRefreshToken = if (responseRefreshToken != null && responseRefreshToken != currentRefreshToken) {
                        Log.d("TokenAuthenticator", "refreshToken이 변경되었습니다: $currentRefreshToken -> $responseRefreshToken")
                        responseRefreshToken
                    } else {
                        Log.d("TokenAuthenticator", "refreshToken 유지: $currentRefreshToken")
                        currentRefreshToken
                    }

                    Log.d("TokenAuthenticator", "토큰 저장 시작")
                    tokenManager.saveTokensSync(newAccessToken, finalRefreshToken)
                    Log.d("TokenAuthenticator", "토큰 저장 완료")

                    Log.d("TokenAuthenticator", "갱신 accessToken : $newAccessToken\n갱신 refreshToken : $finalRefreshToken")

                    return response.request.createRequestWithRenewedToken(newAccessToken)

                } catch (e: Exception) {
                    Log.e("TokenAuthenticator", "synchronized 블록 내 오류", e)
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "토큰 갱신 실패: ${e.message}")
            tokenManager.clearTokensSync()
            runBlocking {
                tokenExpirationNotifier.notifyTokenExpired()
            }
            Log.d("TokenAuthenticator", "authenticate: 세션 만료")
            null
        }
    }

    private fun Request.createRequestWithRenewedToken(renewedToken: String): Request = newBuilder()
        .header("Authorization", "Bearer $renewedToken")
        .build()
}