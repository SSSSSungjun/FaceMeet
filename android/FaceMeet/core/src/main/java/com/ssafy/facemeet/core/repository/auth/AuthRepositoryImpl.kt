package com.ssafy.facemeet.core.repository.auth

import com.ssafy.facemeet.core.constant.AuthStatus
import com.ssafy.facemeet.core.local.datastore.TokenManager
import com.ssafy.facemeet.core.network.api.AuthApiService
import com.ssafy.facemeet.core.network.PersistentCookieJar
import com.ssafy.facemeet.core.network.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.network.dto.request.SignUpRequest
import com.ssafy.facemeet.core.network.dto.response.AuthResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun checkAuthStatus(): AuthStatus {
        val accessToken = tokenManager.getAccessToken()

        // 토큰이 있으면 로그인 상태 (만료는 API 호출 시 401로 판단)
        return if (accessToken.isNullOrEmpty()) {
            AuthStatus.NEED_LOGIN
        } else {
            AuthStatus.LOGGED_IN
        }
    }

    override suspend fun refreshToken(): Result<AuthResponse<Unit>> {
        return runCatching {
            val refreshToken = tokenManager.getRefreshToken()
                ?: throw Exception("No refresh token available")

            val request = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                if (authResponse.status == 200 && !authResponse.accessToken.isNullOrEmpty()) {
                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken!!,
                        refreshToken = refreshToken
                    )
                } else {
                    throw Exception("Token refresh failed: status=${authResponse.status}")
                }

                authResponse
            } else {
                throw Exception("API call failed: ${response.code()}")
            }
        }.onFailure {
            tokenManager.clearTokens()
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }

    override suspend fun signUp(
        nickname: String?,
        address: String?,
        latitude: Float,
        longitude: Float,
        preferAgeLower: Int,
        preferAgeUpper: Int
    ): Result<AuthResponse<Unit>> {
        return runCatching {
            val request = SignUpRequest(nickname, address, latitude, longitude, preferAgeLower, preferAgeUpper)
            val response = authApi.signUp(request)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("SignUp failed: ${response.code()}")
            }
        }
    }

    override suspend fun logout(): Result<AuthResponse<Unit>> {
        return runCatching {
            val response = authApi.logout()

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.clearTokens()
                authResponse
            } else {
                tokenManager.clearTokens() // 실패해도 로컬 토큰은 클리어
                throw Exception("Logout failed: ${response.code()}")
            }
        }.onFailure {
            tokenManager.clearTokens()
        }
    }

    override fun isLoggedInFlow(): Flow<Boolean> = tokenManager.isLoggedInFlow()
}