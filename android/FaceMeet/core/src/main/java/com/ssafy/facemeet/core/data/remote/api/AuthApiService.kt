package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/onboarding")
    suspend fun postOnboarding(@Body request: OnboardingRequest): Response<AuthResponse<Unit>>

    @POST("/api/v1/auth/refresh")
    suspend fun postRefreshToken(@Body request : RefreshTokenRequest): Response<AuthResponse<Unit>>

    @POST("/api/v1/auth/logout")
    suspend fun postLogout(): Response<AuthResponse<Unit>>

}