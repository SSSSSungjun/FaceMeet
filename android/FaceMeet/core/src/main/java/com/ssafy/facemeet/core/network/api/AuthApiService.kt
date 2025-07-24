package com.ssafy.facemeet.core.network.api

import com.ssafy.facemeet.core.network.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.network.dto.request.SignUpRequest
import com.ssafy.facemeet.core.network.dto.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    //api 정의하는 interface

    @POST("api/v1/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse<Unit>>

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request : RefreshTokenRequest): Response<AuthResponse<Unit>>

    @POST("/api/v1/auth/logout")
    suspend fun logout(): Response<AuthResponse<Unit>>

}