package com.ssafy.facemeet.core.repository.auth

import com.ssafy.facemeet.core.network.ApiService
import com.ssafy.facemeet.core.network.PersistentCookieJar
import com.ssafy.facemeet.core.network.dto.request.SignUpRequest
import com.ssafy.facemeet.core.network.dto.response.AuthResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val cookieJar: PersistentCookieJar
) : AuthRepository {

    override suspend fun signUp(
        nickname: String?,
        address: String?,
        latitude: Float,
        longitude: Float,
        preferAgeLower: Int,
        preferAgeUpper: Int
    ): Result<AuthResponse<Unit>> {
        return runCatching {
            val response = apiService.signUp(
                SignUpRequest(nickname, address, latitude, longitude, preferAgeLower, preferAgeUpper)
            )

            response.body()?.takeIf { response.isSuccessful }
                ?: throw Exception("회원가입 실패: ${response.message()}")
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        return runCatching {
            val response = apiService.refreshToken()
            val responseBody = response.body()

            if (response.isSuccessful && responseBody?.status == 200) {
                Unit // refresh 성공
            } else {
                throw Exception("토큰 갱신 실패")
            }
        }.onFailure {
            cookieJar.clearCookies()
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            apiService.logout()
        }.onSuccess {
            cookieJar.clearCookies()
        }.onFailure {
            cookieJar.clearCookies()
        }.map { Unit }
    }

    // 추가: 앱 시작시 쿠키 유효성 확인용
    override suspend fun checkAuthStatus(): Boolean {
        return refreshToken().isSuccess
    }
}