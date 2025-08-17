package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.AuthApiService
import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.AuthResponse
import retrofit2.Response
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService
) {

    suspend fun postOnboarding(request: OnboardingRequest): Response<AuthResponse<Unit>> {
        return authApiService.postOnboarding(request)
    }

    suspend fun postRefreshToken(request: RefreshTokenRequest): Response<AuthResponse<Unit>> {
        return authApiService.postRefreshToken(request)
    }

    suspend fun postLogout(): Response<AuthResponse<Unit>> {
        return authApiService.postLogout()
    }
}
