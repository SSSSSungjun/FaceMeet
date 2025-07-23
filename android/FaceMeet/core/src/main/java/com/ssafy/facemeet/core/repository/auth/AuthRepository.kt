package com.ssafy.facemeet.core.repository.auth

import com.ssafy.facemeet.core.constant.AuthStatus
import com.ssafy.facemeet.core.network.dto.response.AuthResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        nickname: String?,
        address: String?,
        latitude: Float,
        longitude: Float,
        preferAgeLower: Int,
        preferAgeUpper: Int
    ): Result<AuthResponse<Unit>>

    suspend fun logout(): Result<AuthResponse<Unit>>

    suspend fun refreshToken(): Result<AuthResponse<Unit>>  // accessToken 받기 위해

    suspend fun checkAuthStatus(): AuthStatus

    suspend fun saveTokens(accessToken: String, refreshToken: String)

    fun isLoggedInFlow(): Flow<Boolean>
}