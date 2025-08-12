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

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService,
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

                val newTokenResponse = runBlocking {
                    authApiService.postRefreshToken(RefreshTokenRequest(refreshToken = currentRefreshToken))
                }

                if (!newTokenResponse.isSuccessful) {
                    throw Exception("토큰 갱신 실패")
                }

                val newTokenData = newTokenResponse.body() ?: throw Exception("토큰 갱신 응답 에러")

                val newAccessToken = newTokenData.accessToken ?: throw Exception("새 accessToken이 null")
                val newRefreshToken = currentRefreshToken

                tokenManager.saveTokensSync(newAccessToken, newRefreshToken)

                Log.d("TokenAuthenticator", "갱신 accessToken : $newAccessToken\n갱신 refreshToken : $newRefreshToken")

                return response.request.createRequestWithRenewedToken(newAccessToken)
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